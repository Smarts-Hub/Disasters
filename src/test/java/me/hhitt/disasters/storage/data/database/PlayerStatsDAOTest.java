package me.hhitt.disasters.storage.data.database;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.storage.data.PlayerStats;
import me.hhitt.disasters.storage.data.cache.Cache;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerStatsDAOTest {

    @TempDir
    Path tempDir;

    private PlayerStatsDAO dao;
    private Cache cache;

    @BeforeEach
    void setUp() throws Exception {
        cache = new Cache();
        tempDir.resolve("data").toFile().mkdirs();

        Configuration config = mock(Configuration.class);
        when(config.getString("database.driver")).thenReturn("H2");
        when(config.getString("database.db-name")).thenReturn("testdisasters");
        when(config.getString("database.host")).thenReturn(null);
        when(config.getString("database.name")).thenReturn(null);
        when(config.getString("database.user")).thenReturn(null);
        when(config.getString("database.username")).thenReturn(null);
        when(config.getString("database.password")).thenReturn(null);
        when(config.getInt("database.port", 3306)).thenReturn(3306);

        var fmStatic = mockStatic(FileManager.class);
        fmStatic.when(() -> FileManager.get("config")).thenReturn(config);

        var dStatic = mockStatic(Disasters.class);
        Disasters plugin = mock(Disasters.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        Logger testLogger = Logger.getLogger("DisastersTest");
        when(plugin.getLogger()).thenReturn(testLogger);
        dStatic.when(Disasters::getInstance).thenReturn(plugin);

        try {
            dao = new PlayerStatsDAO(cache);
        } finally {
            fmStatic.close();
            dStatic.close();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (dao != null) {
            dao.close();
        }
    }

    private static Configuration mockConfig() {
        Configuration config = mock(Configuration.class);
        when(config.getString("database.driver")).thenReturn("H2");
        when(config.getString("database.db-name")).thenReturn("testdisasters");
        return config;
    }

    @Test
    void ensureAndLoadCreatesNewPlayer() throws SQLException {
        UUID playerId = UUID.randomUUID();
        PlayerStats stats = dao.ensureAndLoad(playerId);

        assertNotNull(stats);
        assertEquals(0, stats.getWins());
        assertEquals(0, stats.getDefeats());
        assertEquals(0, stats.getTotalPlayed());

        PlayerStats cached = cache.getPlayerStats(playerId);
        assertNotNull(cached);
        assertEquals(stats, cached);
    }

    @Test
    void ensureAndLoadReturnsExistingStats() throws SQLException {
        UUID playerId = UUID.randomUUID();
        dao.ensureAndLoad(playerId);

        applyMatchOutcome(Set.of(playerId), Set.of());

        PlayerStats stats = dao.ensureAndLoad(playerId);
        assertEquals(1, stats.getWins());
        assertEquals(0, stats.getDefeats());
        assertEquals(1, stats.getTotalPlayed());
    }

    @Test
    void loadReturnsExistingStats() throws SQLException {
        UUID playerId = UUID.randomUUID();
        dao.ensureAndLoad(playerId);

        PlayerStats stats = dao.load(playerId);
        assertNotNull(stats);
        assertEquals(0, stats.getWins());
    }

    @Test
    void loadFallsBackToEnsureAndLoad() throws SQLException {
        UUID playerId = UUID.randomUUID();
        PlayerStats stats = dao.load(playerId);
        assertNotNull(stats);
        assertEquals(0, stats.getWins());
    }

    @Test
    void applyMatchOutcomeIncrementsWinners() throws SQLException {
        UUID winner = UUID.randomUUID();
        UUID loser = UUID.randomUUID();
        dao.ensureAndLoad(winner);
        dao.ensureAndLoad(loser);

        applyMatchOutcome(Set.of(winner), Set.of(loser));

        PlayerStats winnerStats = dao.load(winner);
        assertEquals(1, winnerStats.getWins());
        assertEquals(0, winnerStats.getDefeats());
        assertEquals(1, winnerStats.getTotalPlayed());

        PlayerStats loserStats = dao.load(loser);
        assertEquals(0, loserStats.getWins());
        assertEquals(1, loserStats.getDefeats());
        assertEquals(1, loserStats.getTotalPlayed());
    }

    @Test
    void applyMatchOutcomeHandlesNewPlayers() throws SQLException {
        UUID winner = UUID.randomUUID();
        UUID loser = UUID.randomUUID();

        applyMatchOutcome(Set.of(winner), Set.of(loser));

        PlayerStats winnerStats = dao.load(winner);
        assertEquals(1, winnerStats.getWins());
        assertEquals(0, winnerStats.getDefeats());
        assertEquals(1, winnerStats.getTotalPlayed());
    }

    @Test
    void applyMatchOutcomeRejectsOverlappingSets() {
        UUID player = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
            applyMatchOutcome(Set.of(player), Set.of(player)));
    }

    @Test
    void applyMatchOutcomeUpdatesCache() throws SQLException {
        UUID winner = UUID.randomUUID();
        UUID loser = UUID.randomUUID();
        dao.ensureAndLoad(winner);
        dao.ensureAndLoad(loser);

        applyMatchOutcome(Set.of(winner), Set.of(loser));

        assertEquals(1, cache.getPlayerStats(winner).getWins());
        assertEquals(1, cache.getPlayerStats(loser).getDefeats());
    }

    @Test
    void multipleMatchesAccumulate() throws SQLException {
        UUID player = UUID.randomUUID();
        dao.ensureAndLoad(player);

        applyMatchOutcome(Set.of(player), Set.of());
        applyMatchOutcome(Set.of(), Set.of(player));
        applyMatchOutcome(Set.of(player), Set.of());

        PlayerStats stats = dao.load(player);
        assertEquals(2, stats.getWins());
        assertEquals(1, stats.getDefeats());
        assertEquals(3, stats.getTotalPlayed());
    }

    private void applyMatchOutcome(Set<UUID> winners, Set<UUID> losers) throws SQLException {
        dao.applyMatchOutcome(new HashSet<>(winners), new HashSet<>(losers));
    }
}
