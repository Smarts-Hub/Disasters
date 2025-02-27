package me.hhitt.disasters.arena

import me.hhitt.disasters.Disasters
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class ArenaManager {

    private val plugin = Disasters.getInstance()
    private val arenas = mutableListOf<Arena>()

    init {
        loadArenas()
    }

    private fun loadArenas() {
        val arenasFolder = File(plugin.dataFolder, "Arenas")

        // Check if the folder exists
        if (!arenasFolder.exists() || !arenasFolder.isDirectory) {
            plugin.logger.severe("Arenas folder does not exist or is not a directory.")
            return
        }

        // List all files in the Arenas folder
        val arenaFiles = arenasFolder.listFiles { _, name -> name.endsWith(".yml", ignoreCase = true) } ?: return
        if (arenaFiles.isEmpty()) {
            plugin.logger.warning("No arena files found in the Arenas folder.")
            return
        }

        for (arenaFile in arenaFiles) {
            val arenaConfig = YamlConfiguration.loadConfiguration(arenaFile)
            val arenaID = arenaFile.nameWithoutExtension
            val gameTime = arenaConfig.getInt("game-time")
            val countdown = arenaConfig.getInt("countdown")
            val maxPlayers = arenaConfig.getInt("max-players")
            val minPlayers = arenaConfig.getInt("min-players")
            val aliveToEnd = arenaConfig.getInt("alive-to-end")
            val disasterRate = arenaConfig.getInt("disaster-rate")
            val maxDisasters = arenaConfig.getInt("max-disasters")
            val displayName = arenaConfig.getString("display-name")!!
            val location = Location(
                plugin.server.getWorld(arenaConfig.getString("world")!!),
                arenaConfig.getDouble("x"),
                arenaConfig.getDouble("y"),
                arenaConfig.getDouble("z"),
                arenaConfig.getInt("yaw").toFloat(),
                arenaConfig.getInt("pitch").toFloat()
            )
            val corner1 = Location(
                plugin.server.getWorld(arenaConfig.getString("world")!!),
                arenaConfig.getDouble("corner1.x"),
                arenaConfig.getDouble("corner1.y"),
                arenaConfig.getDouble("corner1.z")
            )
            val corner2 = Location(
                plugin.server.getWorld(arenaConfig.getString("world")!!),
                arenaConfig.getDouble("corner2.x"),
                arenaConfig.getDouble("corner2.y"),
                arenaConfig.getDouble("corner2.z")
            )

            val arena = Arena(arenaID, displayName, minPlayers, maxPlayers, aliveToEnd, gameTime, countdown, disasterRate, maxDisasters, location, corner1, corner2)

            arenas.add(arena)
        }
    }

    fun getArenas(): List<Arena> = arenas

    fun getArena(player: Player): Arena? {
        return arenas.firstOrNull { it.isPlayerValid(player) }
    }

    fun getArena(arenaId: String): Arena? {
        return arenas.firstOrNull { it.name.equals(arenaId, ignoreCase = true) }
    }

    fun addPlayerToBestArena(player: Player) {
        val bestArena = arenas
            .filter { it.isWaiting() }
            .sortedWith(compareByDescending<Arena> { it.isFull() }.thenBy { it.isEmpty() })
            .firstOrNull()

        bestArena?.addPlayer(player)
    }

    fun reloadArena(arenaFile: File) {
        val arenaID = arenaFile.nameWithoutExtension
        val arena = getArena(arenaID)
        arena?.let { loadArenas() }
    }

    fun removeArena(arenaID: String): Boolean {
        val arena = getArena(arenaID) ?: return false
        return arenas.remove(arena)
    }

}