package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class PlayerDamageListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        val arena = arenaManager.getArena(victim) ?: return
        val attackerArena = arenaManager.getArena(attacker)

        if (arena != attackerArena) {
            event.isCancelled = true
            return
        }

        // Player-vs-player damage is disabled by default after PvP vote removal.
        // Future PvP-like game modifications must expose explicit allow logic through GameModificationRegistry.
        event.isCancelled = true
    }
}
