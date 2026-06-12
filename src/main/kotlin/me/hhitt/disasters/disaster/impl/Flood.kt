package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Notify
import org.bukkit.Material

class Flood : Disaster {

    private val arenas = mutableListOf<Arena>()
    private val currentY = mutableMapOf<Arena, Int>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        val minY = kotlin.math.min(arena.corner1.blockY, arena.corner2.blockY)
        currentY[arena] = minY
        Notify.disaster(arena, "flood")
    }

    override fun pulse(time: Int) {
        val config = FileManager.get("config")
        val riseInterval = config?.getInt("disasters.per-disaster.flood.rise-interval-seconds", 4) ?: 4
        val maxBlocks = config?.getInt("disasters.per-disaster.flood.max-blocks-per-pulse", 2500) ?: 2500

        if (time % riseInterval != 0) return
        arenas.toList().forEach { arena ->
            val y = currentY[arena] ?: return@forEach
            fillLayer(arena, y, maxBlocks)
            currentY[arena] = y + 1
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        currentY.remove(arena)
    }

    private fun fillLayer(arena: Arena, y: Int, maxBlocks: Int) {
        val world = arena.corner1.world ?: return
        val minX = kotlin.math.min(arena.corner1.blockX, arena.corner2.blockX)
        val maxX = kotlin.math.max(arena.corner1.blockX, arena.corner2.blockX)
        val minZ = kotlin.math.min(arena.corner1.blockZ, arena.corner2.blockZ)
        val maxZ = kotlin.math.max(arena.corner1.blockZ, arena.corner2.blockZ)
        var placed = 0
        val xRange = (maxX - minX).coerceAtLeast(1)
        val zRange = (maxZ - minZ).coerceAtLeast(1)
        val step = (((xRange.toLong() * zRange) / maxBlocks.toLong()).coerceAtLeast(1L)).toInt()
        var ix = 0
        while (ix <= xRange && placed < maxBlocks) {
            var iz = 0
            while (iz <= zRange && placed < maxBlocks) {
                val block = world.getBlockAt(minX + ix, y, minZ + iz)
                val type = block.type
                if ((type.isAir || type == Material.CAVE_AIR || type == Material.VOID_AIR)
                    && y + 1 < world.maxHeight
                ) {
                    block.setType(Material.WATER)
                    placed++
                }
                iz += step
            }
            ix += step
        }
    }
}
