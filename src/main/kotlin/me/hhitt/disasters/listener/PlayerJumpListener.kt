package me.hhitt.disasters.listener

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerJumpListener(private val arenaManager: ArenaManager): Listener {

    @EventHandler
    fun onPlayerJump(event: PlayerJumpEvent) {
        val player = event.player
        arenaManager.getArena(player)?.let {
            if(DisasterRegistry.isGrounded(it, player)){
                event.isCancelled = true
            }
        }
    }

}