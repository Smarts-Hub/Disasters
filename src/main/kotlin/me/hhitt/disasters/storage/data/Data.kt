package me.hhitt.disasters.storage.data

import me.hhitt.disasters.storage.data.cache.Cache
import me.hhitt.disasters.storage.data.database.PlayerStatsDAO
import java.util.UUID

object Data {
    private val cache = Cache()
    private val playerStatsDAO = PlayerStatsDAO(cache)


    suspend fun increaseWins(playerId: UUID) {
        val stats = getPlayerStats(playerId)
        updatePlayerStats(playerId, stats.copy(wins = stats.wins + 1))
    }

    suspend fun increaseDefeats(playerId: UUID) {
        val stats = getPlayerStats(playerId)
        updatePlayerStats(playerId, stats.copy(defeats = stats.defeats + 1))
    }

    suspend fun increaseTotalPlayed(playerId: UUID) {
        val stats = getPlayerStats(playerId)
        updatePlayerStats(playerId, stats.copy(totalPlayed = stats.totalPlayed + 1))
    }

    suspend fun getWins(playerId: UUID): Int {
        return getPlayerStats(playerId).wins
    }

    suspend fun getDefeats(playerId: UUID): Int {
        return getPlayerStats(playerId).defeats
    }

    suspend fun getTotalPlayed(playerId: UUID): Int {
        return getPlayerStats(playerId).totalPlayed
    }

    suspend fun loadPlayerAtCache(playerId: UUID) {
        cache.loadPlayerStats(playerId, getPlayerStats(playerId))
    }

    fun unloadPlayerFromCache(playerId: UUID) {
        cache.removePlayerStats(playerId)
    }

    suspend fun createPlayerStats(playerId: UUID) {
        playerStatsDAO.createPlayerStats(playerId)
    }

    suspend fun deletePlayerStats(playerId: UUID) {
        playerStatsDAO.deletePlayer(playerId)
    }

    private suspend fun getPlayerStats(playerId: UUID): PlayerStats {
        return playerStatsDAO.getPlayerStats(playerId)
    }

    private suspend fun updatePlayerStats(playerId: UUID, newStats: PlayerStats) {
        playerStatsDAO.updatePlayerStats(playerId, newStats)
    }
}
