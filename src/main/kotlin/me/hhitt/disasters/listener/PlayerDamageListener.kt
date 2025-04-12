package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class PlayerDamageListener(private val arenaManager: ArenaManager): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        // If the entity is not a player, we don't care and return
        if (event.entity !is Player) {
            return
        }

        val player = event.entity as Player

        // If player is in an arena
        arenaManager.getArena(player)?.let { arena ->
            val damage = event.finalDamage
            val health = player.health

            // Check if the player will die
            if(health - damage > 0) {
                return
            }

            // In this point the player die, then we remove the player from the arena
            // and update the player statistics. Also manage the killer
            arena.playerDied(player)
            Data.increaseDefeats(player.uniqueId)
            Data.increaseTotalPlayed(player.uniqueId)

            // If the killer is not a player
            if(event.damager !is Player) {
                Notify.playerDied(player, arena)
                return
            }

            // If the killer is a player
            val damager = event.damager as Player
            Data.increaseWins(damager.uniqueId)
            Notify.playerKilled(player, damager, arena)

        }
    }

}