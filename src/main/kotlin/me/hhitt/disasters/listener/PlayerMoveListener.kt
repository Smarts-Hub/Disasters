package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.disaster.impl.FloorIsLava
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMoveListener(private val arenaManager: ArenaManager): Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from.block != event.to.block) {

            // If player is in the same block, we don't care and return
            if (event.from.blockX == event.to.blockX &&
                event.from.blockY == event.to.blockY &&
                event.from.blockZ == event.to.blockZ) {
                return
            }

            // Obtaining the arena if player is in one
            arenaManager.getArena(event.player)?.let { arena ->

                if (arena.isWaiting()) {
                    return
                }

                if(!arena.disasters.contains(FloorIsLava())) {
                    return
                }

                // Check if player is in the border and add the block to the floor is lava if needed
                if(arena.borderService.isLocationInArenaTp(event.player)) {
                    DisasterRegistry.addBlockToFloorIsLava(arena, event.to)
                }

            }

        }

    }


}