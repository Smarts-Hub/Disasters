package me.hhitt.disasters.disaster

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.impl.*
import me.hhitt.disasters.game.GameMode
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
        MeteorShower::class,
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

    // Disasters that require PvP - everything else is PvE-safe by default
    private val pvpDisasters = setOf(
        AllowFight::class,
        Murder::class,
        Swap::class
    )

    private inline fun <reified T : Disaster> getDisaster(arena: Arena): T? {
        return activeDisasters[arena]?.find { it is T } as? T
    }

    fun addRandomDisaster(arena: Arena) {
        val maxDisasters = arena.maxDisasters * arena.disasterMultiplier
        val currentDisasters = activeDisasters.getOrPut(arena) { CopyOnWriteArrayList() }

        if (currentDisasters.size >= maxDisasters) {
            // CopyOnWriteArrayList handles removal safely even if pulseAll is running
            val toRemove = currentDisasters.removeAt(0)
            toRemove.stop(arena)
        }

        var available = disasterClasses.filter { cls ->
            currentDisasters.none { it::class == cls }
        }

        // Filter out PvP disasters if arena is in PvE mode
        if (arena.gameMode == GameMode.PVE) {
            available = available.filter { it !in pvpDisasters }
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

    /**
     * Finds the solid block under the player's feet.
     * If player Y has a decimal (on a slab/stair), checks foot level first.
     * Otherwise subtracts 1.0 like normal for full blocks.
     */
    private fun getBlockUnderPlayer(location: Location): Location? {
        // If Y has a decimal, player may be on a half block — check foot level first
        if (location.y % 1.0 != 0.0) {
            val atFeet = location.clone()
            if (!atFeet.block.type.isAir && atFeet.block.type.isSolid) {
                return atFeet
            }
        }
        // Full block — check one block below
        val blockBelow = location.clone().subtract(0.0, 1.0, 0.0)
        if (!blockBelow.block.type.isAir) {
            return blockBelow
        }
        return null
    }

    fun addBlockToDisappear(arena: Arena, location: Location) {
        // Skip if player is on a ladder/vine — don't break blocks while climbing
        if (isPlayerOnClimbable(location)) return

        val block = getBlockUnderPlayer(location) ?: return

        val disaster = activeDisasters[arena]?.find { it is BlockDisappear } as? BlockDisappear
        if (disaster != null) {
            disaster.addBlock(arena, block)
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

        val block = getBlockUnderPlayer(location) ?: return

        // Don't turn water or lava into wool - lava
        val blockType = block.block.type
        if (blockType == Material.WATER || blockType == Material.LAVA) return

        val floorBlock = DisasterFloor(arena, block)
        getDisaster<FloorIsLava>(arena)?.addBlock(floorBlock)
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