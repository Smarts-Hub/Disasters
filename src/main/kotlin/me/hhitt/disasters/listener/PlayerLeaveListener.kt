package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.data.Data
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerLeaveListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        Data.unloadPlayerFromCache(event.player.uniqueId)

        arenaManager.getArena(event.player)?.let { arena ->
            arena.removePlayer(event.player)
        }

    }

}