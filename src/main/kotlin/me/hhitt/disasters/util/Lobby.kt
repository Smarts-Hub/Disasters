package me.hhitt.disasters.util

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player

object Lobby {

    /*
     * May this class is not the best way to set the lobby location,
     * but is really helpful.
     */

    private var location: Location = Location(null, 0.0, 0.0, 0.0)

    fun setLocation() {
        val loc = Location(
            Bukkit.getWorld(FileManager.get("config")!!.getString("lobby.world")!!),
            FileManager.get("config")!!.getDouble("lobby.x"),
            FileManager.get("config")!!.getDouble("lobby.y"),
            FileManager.get("config")!!.getDouble("lobby.z"),
            FileManager.get("config")!!.getDouble("lobby.yaw").toFloat(),
            FileManager.get("config")!!.getDouble("lobby.pitch").toFloat()
        )
        this.location = loc
    }

    fun teleportPlayer(player: Player) {
         player.teleport(location)
         player.activePotionEffects.clear()
         player.health = 20.0
        player.gameMode = GameMode.SURVIVAL
     }

    fun teleportAtEnd(arena: Arena) {
        Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), Runnable {
            arena.playing.forEach {
                teleportPlayer(it)
            }
            arena.clear()
        }, 60L)
    }
}