package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.game.modification.GameModificationRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent

class HealthRegenListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onHealthRegen(event: EntityRegainHealthEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        arenaManager.getArena(player)?.let { arena ->
            if (GameModificationRegistry.isActive(arena, "one-heart")) {
                event.isCancelled = true
            }
        }
    }
}
