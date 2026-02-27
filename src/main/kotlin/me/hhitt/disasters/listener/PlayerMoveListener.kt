package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.disaster.impl.BlockDisappear
import me.hhitt.disasters.disaster.impl.FloorIsLava
import me.hhitt.disasters.disaster.impl.Lag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.random.Random

class PlayerMoveListener(private val arenaManager: ArenaManager): Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from.block == event.to.block) {
            return
        }

        // If player is in the same block, we don't care and return
        if (event.from.blockX == event.to.blockX &&
            event.from.blockY == event.to.blockY &&
            event.from.blockZ == event.to.blockZ) {
            return
        }

        val arena = arenaManager.getArena(event.player) ?: return


        if (arena.isWaiting()) {
            return
        }

        if (arena.disasters.any { it is FloorIsLava }) {
            if(arena.borderService.isLocationInArenaTp(event.player)) {
                DisasterRegistry.addBlockToFloorIsLava(arena, event.to)
            }
        }

        if (arena.disasters.any { it is BlockDisappear }) {
            if(arena.borderService.isLocationInArenaTp(event.player)) {
                // Pause cracking on the block the player just left
                val blockBelowFrom = event.from.clone().subtract(0.0, 1.0, 0.0)
                DisasterRegistry.setBlockUnoccupied(arena, blockBelowFrom)

                DisasterRegistry.addBlockToDisappear(arena, event.to)
            }
        }

        if (!arena.disasters.any { it is Lag }) return

        if (Random.nextDouble() > 0.45) return // 45% chance to lag the player
        when (Random.nextInt(5)) {
            0 -> { // Simply lag effect
                event.isCancelled = true
            }

            1 -> { // Go back
                val from = event.from
                val to = event.to ?: return
                val dx = from.x - to.x
                val dz = from.z - to.z
                val back = to.clone().add(dx, 0.0, dz)
                event.to = back
            }
            2 -> { // Go forward
                val from = event.from
                val to = event.to ?: return
                val dx = to.x - from.x
                val dz = to.z - from.z
                val forward = to.clone().add(dx, 0.0, dz)
                event.to = forward
            }
            3 -> { // Random teleport 2 blocks away
                val to = event.to ?: return
                val randX = Random.nextDouble(-2.0, 2.0)
                val randZ = Random.nextDouble(-2.0, 2.0)
                val newLoc = to.clone().add(randX, 0.0, randZ)
                event.player.teleport(newLoc)
            }
            4 -> { //Nothing
            }
        }


    }


}