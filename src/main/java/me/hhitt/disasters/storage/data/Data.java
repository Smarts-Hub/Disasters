package me.hhitt.disasters.storage.data;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.storage.data.cache.Cache;
import me.hhitt.disasters.storage.data.database.PlayerStatsDAO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class Data {

    private static PlayerStatsDAO dao;
    private static Cache cache;
    private static ExecutorService databaseExecutor;
    private static boolean shuttingDown;

    private Data() {
    }

    public static void init() {
        cache = new Cache();
        dao = new PlayerStatsDAO(cache);
        shuttingDown = false;
        databaseExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = new Thread(r, "Disasters-Database");
                thread.setDaemon(false);
                return thread;
            }
        });
    }

    public static CompletableFuture<PlayerStats> loadPlayer(final UUID playerId) {
        if (shuttingDown || databaseExecutor == null) {
            return CompletableFuture.completedFuture(new PlayerStats(0, 0, 0));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return dao.ensureAndLoad(playerId);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load player stats for " + playerId, e);
            }
        }, databaseExecutor).whenComplete((result, throwable) -> {
            if (throwable != null) {
                Disasters.getInstance().getLogger().log(Level.SEVERE,
                    "Database error loading player " + playerId, throwable);
            }
        });
    }

    public static CompletableFuture<Void> recordMatchResults(final Set<UUID> winners, final Set<UUID> losers) {
        if (shuttingDown || databaseExecutor == null) {
            return CompletableFuture.completedFuture(null);
        }
        final Set<UUID> winnersCopy = Collections.unmodifiableSet(new HashSet<UUID>(winners));
        final Set<UUID> losersCopy = Collections.unmodifiableSet(new HashSet<UUID>(losers));
        return CompletableFuture.runAsync(() -> {
            try {
                dao.applyMatchOutcome(winnersCopy, losersCopy);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to record match results", e);
            }
        }, databaseExecutor).whenComplete((result, throwable) -> {
            if (throwable != null) {
                Disasters.getInstance().getLogger().log(Level.SEVERE,
                    "Database error recording match results", throwable);
            }
        });
    }

    public static void unloadPlayerFromCache(final UUID playerId) {
        if (cache != null) {
            cache.removePlayerStats(playerId);
        }
    }

    public static int getWinsFromCache(final UUID playerId) {
        if (cache == null) {
            return 0;
        }
        final PlayerStats stats = cache.getPlayerStats(playerId);
        return stats != null ? stats.getWins() : 0;
    }

    public static int getDefeatsFromCache(final UUID playerId) {
        if (cache == null) {
            return 0;
        }
        final PlayerStats stats = cache.getPlayerStats(playerId);
        return stats != null ? stats.getDefeats() : 0;
    }

    public static int getTotalPlayedFromCache(final UUID playerId) {
        if (cache == null) {
            return 0;
        }
        final PlayerStats stats = cache.getPlayerStats(playerId);
        return stats != null ? stats.getTotalPlayed() : 0;
    }

    public static void shutdown() {
        shuttingDown = true;
        if (databaseExecutor != null) {
            databaseExecutor.shutdown();
            try {
                if (!databaseExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    databaseExecutor.shutdownNow();
                    if (!databaseExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        Disasters.getInstance().getLogger().warning("Database executor did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                databaseExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if (dao != null) {
            try {
                dao.close();
            } catch (SQLException e) {
                Disasters.getInstance().getLogger().log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
}
