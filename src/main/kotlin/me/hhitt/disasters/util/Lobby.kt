package me.hhitt.disasters.util

import me.hhitt.disasters.storage.file.FileManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

object Lobby {

    private var location: Location = Location(null, 0.0, 0.0, 0.0)

    fun setLocation() {
        val loc = Location(
            Bukkit.getWorld(FileManager.get("config")!!.getString("lobby.world")!!),
            FileManager.get("config")!!.getDouble("lobby.x"),
            FileManager.get("config")!!.getDouble("lobby.y"),
            FileManager.get("config")!!.getDouble("lobby.z"),
            FileManager.get("config")!!.get("lobby.yaw") as Float,
            FileManager.get("config")!!.get("lobby.pitch") as Float
        )
        this.location = loc
    }
     fun teleportPlayer(player: Player) {
         player.teleport(location)
     }
}