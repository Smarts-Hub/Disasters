package me.hhitt.disasters.storage.data.database;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.storage.data.PlayerStats;
import me.hhitt.disasters.storage.data.cache.Cache;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerStatsDAO {

    private final Cache cache;
    private final Connection connection;

    public PlayerStatsDAO(final Cache cache) {
        this.cache = cache;

        final Configuration config = FileManager.get("config");
        if (config == null) {
            throw new IllegalStateException("Config file not found!");
        }

        final String dbType = config.getString("database.driver");
        if (dbType == null) {
            throw new IllegalArgumentException("Database driver not specified in config");
        }

        final String driverClass;
        final String url;
        final String user;
        final String password;

        switch (dbType.toLowerCase()) {
            case "mysql": {
                final String host = config.getString("database.host", "localhost");
                final int port = config.getInt("database.port", 3306);
                final String dbName = resolveConfig(config, "database.db-name", "database.name", "disasters");
                user = resolveConfig(config, "database.username", "database.user", "root");
                password = config.getString("database.password", "");
                url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                driverClass = "com.mysql.cj.jdbc.Driver";
                break;
            }
            case "h2": {
                Disasters.getInstance().getLogger().info("Loading H2 database...");
                final String dbName = resolveConfig(config, "database.db-name", "database.name", "disasters");
                final String dataFolder = Disasters.getInstance().getDataFolder().getAbsolutePath();
                url = "jdbc:h2:file:" + dataFolder + "/data/" + dbName + ";DB_CLOSE_DELAY=-1;";
                user = "sa";
                password = "";
                driverClass = "org.h2.Driver";
                break;
            }
            default:
                throw new IllegalArgumentException("Database '" + dbType + "' type is not supported! Available: 'H2', 'MySQL'.");
        }

        try {
            Class.forName(driverClass);
            this.connection = DriverManager.getConnection(url, user, password);
            createTable();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    private static String resolveConfig(final Configuration config, final String primary, final String fallback, final String defaultValue) {
        String value = config.getString(primary);
        if (value != null) {
            return value;
        }
        value = config.getString(fallback);
        return value != null ? value : defaultValue;
    }

    private void createTable() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS players (id VARCHAR(36) PRIMARY KEY, wins INT NOT NULL DEFAULT 0, defeats INT NOT NULL DEFAULT 0, total_played INT NOT NULL DEFAULT 0)"
        )) {
            stmt.executeUpdate();
        }
    }

    public PlayerStats ensureAndLoad(final UUID playerId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT wins, defeats, total_played FROM players WHERE id = ?"
        )) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final PlayerStats stats = new PlayerStats(
                        rs.getInt("wins"),
                        rs.getInt("defeats"),
                        rs.getInt("total_played")
                    );
                    cache.updatePlayerStats(playerId, stats);
                    return stats;
                }
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO players (id, wins, defeats, total_played) VALUES (?, 0, 0, 0)"
        )) {
            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        }

        final PlayerStats stats = new PlayerStats(0, 0, 0);
        cache.updatePlayerStats(playerId, stats);
        return stats;
    }

    public PlayerStats load(final UUID playerId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT wins, defeats, total_played FROM players WHERE id = ?"
        )) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final PlayerStats stats = new PlayerStats(
                        rs.getInt("wins"),
                        rs.getInt("defeats"),
                        rs.getInt("total_played")
                    );
                    cache.updatePlayerStats(playerId, stats);
                    return stats;
                }
            }
        }
        return ensureAndLoad(playerId);
    }

    public void applyMatchOutcome(final Set<UUID> winners, final Set<UUID> losers) throws SQLException {
        final Set<UUID> intersection = new HashSet<UUID>(winners);
        intersection.retainAll(losers);
        if (!intersection.isEmpty()) {
            throw new IllegalArgumentException("Winner and loser sets must be disjoint");
        }

        final boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            for (final UUID winner : winners) {
                final int updated = executeUpdateOrInsert(
                    "UPDATE players SET wins = wins + 1, total_played = total_played + 1 WHERE id = ?",
                    winner
                );
                if (updated == 0) {
                    try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO players (id, wins, defeats, total_played) VALUES (?, 1, 0, 1)"
                    )) {
                        stmt.setString(1, winner.toString());
                        stmt.executeUpdate();
                    }
                }
            }
            for (final UUID loser : losers) {
                final int updated = executeUpdateOrInsert(
                    "UPDATE players SET defeats = defeats + 1, total_played = total_played + 1 WHERE id = ?",
                    loser
                );
                if (updated == 0) {
                    try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO players (id, wins, defeats, total_played) VALUES (?, 0, 1, 1)"
                    )) {
                        stmt.setString(1, loser.toString());
                        stmt.executeUpdate();
                    }
                }
            }

            connection.commit();

            for (final UUID winner : winners) {
                refreshCache(winner);
            }
            for (final UUID loser : losers) {
                refreshCache(loser);
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                // ignore restore failure
            }
        }
    }

    private int executeUpdateOrInsert(final String updateSql, final UUID playerId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, playerId.toString());
            return stmt.executeUpdate();
        }
    }

    private void refreshCache(final UUID playerId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT wins, defeats, total_played FROM players WHERE id = ?"
        )) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final PlayerStats stats = new PlayerStats(
                        rs.getInt("wins"),
                        rs.getInt("defeats"),
                        rs.getInt("total_played")
                    );
                    cache.updatePlayerStats(playerId, stats);
                }
            }
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
