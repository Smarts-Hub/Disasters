package me.hhitt.disasters.listener

import me.hhitt.disasters.storage.data.Data
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener: Listener {

    @EventHandler
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        Data.createPlayerStats(event.player.uniqueId)
        Data.loadPlayerAtCache(event.player.uniqueId)
    }

}