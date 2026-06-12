package me.hhitt.disasters.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Notify
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        arenaManager.getArena(player)?.let { arena ->
            arena.playerDied(player)
            event.deathMessage(DeathMessageService.messageFor(player))
            DeathMessageService.clear(player)
            Notify.playerDied(player, arena)
            val playerId = player.uniqueId
            Disasters.getInstance().launch {
                Data.increaseDefeats(playerId)
                Data.increaseTotalPlayed(playerId)
            }
        }
    }
}
