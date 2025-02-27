package me.hhitt.disasters.listener

import me.hhitt.disasters.storage.data.Data
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerLeaveListener: Listener {

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        Data.unloadPlayerFromCache(event.player.uniqueId)
    }

}