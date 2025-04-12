package me.hhitt.disasters.storage.data.cache

import me.hhitt.disasters.storage.data.PlayerStats
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache class to store player statistics in memory.
 * This is a simple in-memory cache that uses a ConcurrentHashMap to store player statistics.
 * It is not persistent and will be cleared when the server restarts.
 */

class Cache {
    private val playerStatsCache = ConcurrentHashMap<UUID, PlayerStats>()

    fun getPlayerStats(playerId: UUID): PlayerStats? {
        return playerStatsCache[playerId]
    }

    fun updatePlayerStats(playerId: UUID, stats: PlayerStats) {
        playerStatsCache[playerId] = stats
    }

    fun loadPlayerStats(playerId: UUID, stats: PlayerStats){
        playerStatsCache[playerId] = stats
    }

    fun removePlayerStats(playerId: UUID) {
        playerStatsCache.remove(playerId)
    }
}