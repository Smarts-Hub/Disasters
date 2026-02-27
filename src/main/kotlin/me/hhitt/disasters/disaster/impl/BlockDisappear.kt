package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.model.block.DisappearBlock
import me.hhitt.disasters.util.Notify
import java.util.concurrent.ConcurrentHashMap

class BlockDisappear : Disaster {

    // Keyed by block location string so we can look up existing blocks for pause/resume
    private val blocks = ConcurrentHashMap<String, DisappearBlock>()

    // Duration in seconds — disaster stops accepting new blocks after this
    private val duration = 120
    private var elapsed = 0
    private var active = true

    override fun start(arena: Arena) {
        Notify.disaster(arena, "disappear-blocks")
    }

    override fun pulse(time: Int) {
        elapsed++
        // Stop accepting new blocks after duration, but let existing ones finish breaking
        if (elapsed >= duration) {
            active = false
        }
        blocks.values.forEach { it.updateMaterial() }
    }

    override fun stop(arena: Arena) {
        blocks.clear()
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