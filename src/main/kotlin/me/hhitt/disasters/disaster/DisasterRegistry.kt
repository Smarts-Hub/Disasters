package me.hhitt.disasters.disaster

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.impl.*
import me.hhitt.disasters.model.block.DisappearBlock
import me.hhitt.disasters.model.block.DisasterFloor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * DisasterRegistry is a singleton object that manages the active disasters in the game.
 * It allows adding, removing, and pulsing disasters for each arena.
 */

object DisasterRegistry {

    // Fixed: Using ConcurrentHashMap and CopyOnWriteArrayList to prevent ConcurrentModificationException
    private val activeDisasters = ConcurrentHashMap<Arena, CopyOnWriteArrayList<Disaster>>()

    private val disasterClasses = listOf(
        AcidRain::class,
        Apocalypse::class,
        Blind::class,
        Cobweb::class,
        Lag::class,
        Wither::class,
        AllowFight::class,
        BlockDisappear::class,
        HotSun::class,
        Murder::class,
        ZeroGravity::class,
        ExplosiveSheep::class,
        FloorIsLava::class,
        Grounded::class,
        Lightning::class,
        OneHearth::class,
        Swap::class,
        WorldBorder::class
    )

    private inline fun <reified T : Disaster> getDisaster(arena: Arena): T? {
        return activeDisasters[arena]?.find { it is T } as? T
    }

    fun addRandomDisaster(arena: Arena) {
        val maxDisasters = arena.maxDisasters
        val currentDisasters = activeDisasters.getOrPut(arena) { CopyOnWriteArrayList() }

        if (currentDisasters.size >= maxDisasters) {
            // CopyOnWriteArrayList handles removal safely even if pulseAll is running
            val toRemove = currentDisasters.removeAt(0)
            toRemove.stop(arena)
        }

        val available = disasterClasses.filter { cls ->
            currentDisasters.none { it::class == cls }
        }

        if (available.isNotEmpty()) {
            val newDisaster = available.random().constructors.first().call()
            newDisaster.start(arena)
            currentDisasters.add(newDisaster)
            arena.disasters.add(newDisaster)
        }
    }

    fun pulseAll(time: Int) {
        // ConcurrentHashMap and CopyOnWriteArrayList provide thread-safe iterators.
        activeDisasters.forEach { (_, disasters) ->
            disasters.forEach { it.pulse(time) }
        }
    }

    fun removeDisasters(arena: Arena) {
        activeDisasters[arena]?.forEach { it.stop(arena) }
        activeDisasters.remove(arena)
        arena.disasters.clear()
    }

    /**
     * Returns true if the player is on a climbable block (ladder, vines, etc.)
     * Both disasters should be skipped when the player is climbing.
     */
    private fun isPlayerOnClimbable(location: Location): Boolean {
        return Tag.CLIMBABLE.isTagged(location.block.type)
    }

    fun addBlockToDisappear(arena: Arena, location: Location) {
        // Skip if player is on a ladder/vine — don't break blocks while climbing
        if (isPlayerOnClimbable(location)) return

        // Fix: Check the block below the player's feet, not the block at foot level (which is air on full blocks)
        val blockBelow = location.clone().subtract(0.0, 1.0, 0.0)
        if (blockBelow.block.type.isAir) return

        val disaster = activeDisasters[arena]?.find { it is BlockDisappear } as? BlockDisappear
        if (disaster != null) {
            disaster.addBlock(arena, blockBelow)
        }
    }

    fun removeBlockFromDisappear(arena: Arena, block: DisappearBlock) {
        getDisaster<BlockDisappear>(arena)?.removeBlock(block)
    }

    // Pauses cracking on a block when the player steps off it
    fun setBlockUnoccupied(arena: Arena, location: Location) {
        getDisaster<BlockDisappear>(arena)?.setUnoccupied(location)
    }

    fun addBlockToFloorIsLava(arena: Arena, location: Location) {
        // Skip if player is on a ladder/vine — don't turn blocks to lava while climbing
        if (isPlayerOnClimbable(location)) return

        // Fix: Check the block below the player's feet, not the block at foot level (which is air on full blocks)
        val blockBelow = location.clone().subtract(0.0, 1.0, 0.0)
        if (blockBelow.block.type.isAir) return

        // Don't turn water or lava into wool - lava
        val blockType = blockBelow.block.type
        if (blockType == Material.WATER || blockType == Material.LAVA) return

        val block = DisasterFloor(arena, blockBelow)
        getDisaster<FloorIsLava>(arena)?.addBlock(block)
    }

    fun removeBlockFromFloorIsLava(arena: Arena, block: DisasterFloor) {
        getDisaster<FloorIsLava>(arena)?.removeBlock(block)
    }

    fun isGrounded(arena: Arena, player: Player): Boolean {
        return getDisaster<Grounded>(arena)?.isGrounded(player) ?: false
    }

    fun isAllowedToFight(arena: Arena, player: Player): Boolean {
        return getDisaster<AllowFight>(arena)?.isAllowed(player) ?: false
    }
    fun isMurder(arena: Arena, player: Player): Boolean {
        return getDisaster<Murder>(arena)?.isMurder(player) ?: false
    }
}