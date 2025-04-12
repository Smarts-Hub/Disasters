package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.obj.block.DisasterFloor
import me.hhitt.disasters.util.Notify
import java.util.concurrent.CopyOnWriteArrayList

class FloorIsLava: Disaster {

    private val blocks = CopyOnWriteArrayList<DisasterFloor>()

    override fun start(arena: Arena) {
        Notify.disaster(arena, "floor-is-lava")
    }

    override fun pulse(time: Int) {
        blocks.forEach { it.updateMaterial() }
    }

    override fun stop(arena: Arena) {
    }

    fun addBlock(block: DisasterFloor) {
        blocks.add(block)
    }

    fun removeBlock(block: DisasterFloor) {
        blocks.remove(block)
    }
}