package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.model.block.DisasterFloor
import me.hhitt.disasters.util.Notify
import java.util.concurrent.ConcurrentHashMap

class FloorIsLava: Disaster {

    // Keyed by block location string to prevent duplicate entries for the same block
    private val blocks = ConcurrentHashMap<String, DisasterFloor>()

    // Duration in seconds — disaster stops accepting new blocks after this
    private val duration = 120
    private var elapsed = 0
    private var active = true

    override fun start(arena: Arena) {
        Notify.disaster(arena, "floor-is-lava")
    }

    override fun pulse(time: Int) {
        elapsed++
        // Stop accepting new blocks after duration, but let existing ones finish transitioning
        if (elapsed >= duration) {
            active = false
        }
        blocks.values.forEach { it.updateMaterial() }
    }

    override fun stop(arena: Arena) {
        blocks.clear()
    }

    fun isActive(): Boolean = active

    fun addBlock(block: DisasterFloor) {
        if (!active) return
        val key = locationKey(block)
        // Don't add if this block is already transitioning
        if (blocks.containsKey(key)) return
        blocks[key] = block
    }

    fun removeBlock(block: DisasterFloor) {
        blocks.remove(locationKey(block))
    }

    private fun locationKey(block: DisasterFloor): String {
        return "${block.location.blockX},${block.location.blockY},${block.location.blockZ}"
    }
}