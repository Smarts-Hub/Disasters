package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.impl.OneHearth
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent

class HealthRegenListener(private val arenaManager: ArenaManager): Listener {

    @EventHandler
    fun onHealthRegen(event: EntityRegainHealthEvent) {
        // If the entity is not a player, we don't care and return
        if (event.entity !is Player) {
            return
        }

        val player = event.entity as Player

        // If player is in an arena
        arenaManager.getArena(player)?.let { arena ->
            if(arena.disasters.contains(OneHearth())) {
                event.isCancelled = true
            }
        }
    }

}