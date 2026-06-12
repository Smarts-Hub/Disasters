package me.hhitt.disasters.storage.data.cache;

import me.hhitt.disasters.storage.data.PlayerStats;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Cache {

    private final ConcurrentMap<UUID, PlayerStats> playerStatsCache = new ConcurrentHashMap<>();

    public PlayerStats getPlayerStats(final UUID playerId) {
        return playerStatsCache.get(playerId);
    }

    public void updatePlayerStats(final UUID playerId, final PlayerStats stats) {
        playerStatsCache.put(playerId, stats);
    }

    public void loadPlayerStats(final UUID playerId, final PlayerStats stats) {
        playerStatsCache.put(playerId, stats);
    }

    public void removePlayerStats(final UUID playerId) {
        playerStatsCache.remove(playerId);
    }
}
