package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.model.block.DisappearBlock
import me.hhitt.disasters.util.Notify
import java.util.concurrent.ConcurrentHashMap

class BlockDisappear : Disaster {

    // Keyed by block location string so we can look up existing blocks for pause/resume
    private val blocks = ConcurrentHashMap<String, DisappearBlock>()
    private var arena: Arena? = null

    // Duration in seconds — disaster stops accepting new blocks after this
    private val duration = 120
    private var elapsed = 0
    private var active = true

    override fun start(arena: Arena) {
        this.arena = arena
        Notify.disaster(arena, "disappear-blocks")
    }

    override fun pulse(time: Int) {
        elapsed++
        // Stop accepting new blocks after duration, but let existing ones finish breaking
        if (elapsed >= duration) {
            active = false
        }
        blocks.values.forEach { it.updateMaterial() }

        // If a player is standing on air, find the closest solid block under them and add it
        val currentArena = arena ?: return
        if (!active) return
        currentArena.alive.toList().forEach { player ->
            val loc = player.location
            val blockBelow = loc.clone().subtract(0.0, 1.0, 0.0)
            if (blockBelow.block.type.isAir) {
                // Player center is over air — check hitbox corners for a solid block nearby
                val offsets = doubleArrayOf(-0.3, 0.3)
                for (dx in offsets) {
                    for (dz in offsets) {
                        val check = loc.clone().add(dx, -1.0, dz)
                        if (!check.block.type.isAir && check.block.type.isSolid) {
                            DisasterRegistry.addBlockToDisappear(currentArena, loc.clone().add(dx, 0.0, dz))
                            return@forEach
                        }
                    }
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        blocks.clear()
        this.arena = null
    }

    fun isActive(): Boolean = active

    /**
     * Adds a block or marks an existing one as occupied (resumes cracking).
     * Uses location key to prevent duplicate entries for the same block.
     */
    fun addBlock(arena: Arena, location: org.bukkit.Location) {
        if (!active) return
        val key = locationKey(location)
        val existing = blocks[key]
        if (existing != null) {
            // Player stepped back on a block that was already cracking — resume
            existing.occupied = true
            return
        }
        val block = DisappearBlock(arena, location)
        blocks[key] = block
    }

    /**
     * Marks a block as unoccupied (pauses breaking) when the player steps off.
     */
    fun setUnoccupied(location: org.bukkit.Location) {
        val key = locationKey(location)
        blocks[key]?.occupied = false
    }

    fun removeBlock(block: DisappearBlock) {
        blocks.remove(locationKey(block.location))
    }

    private fun locationKey(loc: org.bukkit.Location): String {
        return "${loc.blockX},${loc.blockY},${loc.blockZ}"
    }
}