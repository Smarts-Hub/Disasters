package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Notify
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener(private val arenaManager: ArenaManager): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerDeath(event: PlayerDeathEvent) {

        val player = event.player

        arenaManager.getArena(player)?.let { arena ->
            arena.playerDied(player)
            Notify.playerDied(player, arena)
            Data.increaseDefeats(player.uniqueId)
            Data.increaseTotalPlayed(player.uniqueId)
        }
    }

}