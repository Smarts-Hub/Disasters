package me.hhitt.disasters.storage.data.database

import me.hhitt.disasters.storage.data.PlayerStats
import me.hhitt.disasters.storage.data.Players
import me.hhitt.disasters.storage.data.cache.Cache
import me.hhitt.disasters.storage.file.FileManager
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.Dispatchers
import me.hhitt.disasters.Disasters
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

/**
 * Data Access Object (DAO) for managing player statistics in the database.
 * This class is responsible for loading, updating, and retrieving player statistics.
 *
 * @param cache The cache instance used to store player statistics.
 */

class PlayerStatsDAO(private val cache: Cache) {

    init {
        val config = FileManager.get("config") ?: throw IllegalStateException("Config file not found!")
        val dbType = config.getString("database.driver")?.lowercase() ?: throw IllegalArgumentException("Database driver not specified in config")

        when (dbType) {
            "mysql" -> {
                val host = config.getString("database.host") ?: "localhost"
                val port = config.getInt("database.port") ?: 3306
                val databaseName = config.getString("database.name") ?: "database"
                val user = config.getString("database.user") ?: "root"
                val password = config.getString("database.password") ?: ""

                val url = "jdbc:mysql://$host:$port/$databaseName"
                Database.connect(
                    url = url,
                    driver = "com.mysql.cj.jdbc.Driver",
                    user = user,
                    password = password
                )
            }
            "h2" -> {
                Disasters.getInstance().logger.info("Loading H2 database...")
                val databaseName = config.getString("database.name") ?: "database"
                val url = "jdbc:h2:file:./data/$databaseName;DB_CLOSE_DELAY=-1;"
                Database.connect(
                    url = url,
                    driver = "org.h2.Driver",
                    user = "root",
                    password = ""
                )
            }
            else -> throw IllegalArgumentException("Database '$dbType' type is not supported! Available: 'H2', 'MySQL'.")
        }

        transaction {
            Disasters.getInstance().logger.info("Enter at transaction!")
            SchemaUtils.create(Players)
        }
    }

    suspend fun getPlayerStats(playerId: UUID): PlayerStats {
        cache.getPlayerStats(playerId)?.let { return it }

        var stats = newSuspendedTransaction(Dispatchers.IO) {
            Players.selectAll().where { Players.id eq playerId }.singleOrNull()?.let {
                PlayerStats(
                    wins = it[Players.wins],
                    defeats = it[Players.defeats],
                    totalPlayed = it[Players.totalPlayed]
                )
            }
        }

        stats?.let { cache.updatePlayerStats(playerId, it) } ?: run{{
            stats = PlayerStats(0, 0, 0)
        }}

        return stats!!
    }

    suspend fun updatePlayerStats(playerId: UUID, newStats: PlayerStats) {
        newSuspendedTransaction(Dispatchers.IO) {
            Players.update({ Players.id eq playerId }) {
                it[wins] = newStats.wins
                it[defeats] = newStats.defeats
                it[totalPlayed] = newStats.totalPlayed
            }
        }
        cache.updatePlayerStats(playerId, newStats)
    }

    suspend fun createPlayerStats(playerId: UUID) {
        if(hasStats(playerId)) return
        newSuspendedTransaction(Dispatchers.IO) {
            Players.insert {
                it[id] = playerId
                it[wins] = 0
                it[defeats] = 0
                it[totalPlayed] = 0
            }
        }
        cache.updatePlayerStats(playerId, PlayerStats(0, 0, 0))
    }

    suspend fun deletePlayer(playerId: UUID) {
        newSuspendedTransaction(Dispatchers.IO) {
            Players.deleteWhere { id eq playerId }
        }
        cache.removePlayerStats(playerId)
    }

    private suspend fun hasStats(playerId: UUID): Boolean {
        cache.getPlayerStats(playerId)?.let { return true }

        val stats = newSuspendedTransaction(Dispatchers.IO) {
            Players.selectAll().where { Players.id eq playerId }.singleOrNull()?.let {
                true
            }
        }

        return stats ?: false
    }
}
