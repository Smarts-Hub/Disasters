package me.hhitt.disasters.storage.data.cache

import me.hhitt.disasters.storage.data.PlayerStats
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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