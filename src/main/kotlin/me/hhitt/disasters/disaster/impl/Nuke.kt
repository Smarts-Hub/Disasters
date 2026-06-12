package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitTask
import kotlin.math.max
import kotlin.math.min

class Nuke : Disaster, TriggerTrackedDisaster {

    override var triggerCount: Int = 0
        private set

    private val arenas = mutableListOf<Arena>()
    private val tasks = mutableListOf<BukkitTask>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "nuke")
    }

    override fun pulse(time: Int) {
        arenas.toList().forEach { arena ->
            if (time == 5) colorQuadrant(arena, Material.YELLOW_TERRACOTTA)
            if (time == 10) colorQuadrant(arena, Material.ORANGE_TERRACOTTA)
            if (time == 15) {
                colorQuadrant(arena, Material.RED_TERRACOTTA)
                arena.playing.forEach { it.playSound(it.location, Sound.BLOCK_BELL_USE, 1f, 0.5f) }
            }
            if (time == 20) detonate(arena)
        }
    }

    override fun stop(arena: Arena) {
        tasks.forEach { it.cancel() }
        tasks.clear()
        arenas.remove(arena)
    }

    private fun colorQuadrant(arena: Arena, material: Material) {
        val world = arena.corner1.world ?: return
        val centerX = (arena.corner1.blockX + arena.corner2.blockX) / 2
        val centerZ = (arena.corner1.blockZ + arena.corner2.blockZ) / 2
        val minX = min(arena.corner1.blockX, arena.corner2.blockX)
        val maxX = max(arena.corner1.blockX, arena.corner2.blockX)
        val minZ = min(arena.corner1.blockZ, arena.corner2.blockZ)
        val maxZ = max(arena.corner1.blockZ, arena.corner2.blockZ)
        val startX = if (centerX - 0 >= 0) centerX else minX
        val endX = if (centerX - 0 >= 0) maxX else centerX
        val startZ = if (centerZ - 0 >= 0) centerZ else minZ
        val endZ = if (centerZ - 0 >= 0) maxZ else centerZ
        var placed = 0
        val cap = 2500
        outer@ for (x in startX..endX) {
            for (z in startZ..endZ) {
                if (placed >= cap) break@outer
                val y = world.getHighestBlockYAt(x, z)
                val block = world.getBlockAt(x, y, z)
                if (block.type.isSolid) {
                    block.setType(material)
                    placed++
                }
            }
        }
    }

    private fun detonate(arena: Arena) {
        val world = arena.corner1.world ?: return
        val centerX = (arena.corner1.blockX + arena.corner2.blockX) / 2
        val centerZ = (arena.corner1.blockZ + arena.corner2.blockZ) / 2
        val minX = min(arena.corner1.blockX, arena.corner2.blockX)
        val maxX = max(arena.corner1.blockX, arena.corner2.blockX)
        val minZ = min(arena.corner1.blockZ, arena.corner2.blockZ)
        val maxZ = max(arena.corner1.blockZ, arena.corner2.blockZ)
        val startX = if (centerX - 0 >= 0) centerX else minX
        val endX = if (centerX - 0 >= 0) maxX else centerX
        val startZ = if (centerZ - 0 >= 0) centerZ else minZ
        val endZ = if (centerZ - 0 >= 0) maxZ else centerZ
        arena.alive.toList().forEach { player ->
            if (player.location.blockX in startX..endX && player.location.blockZ in startZ..endZ) {
                DeathMessageService.mark(player, "nuke")
            }
        }
        var x = startX
        while (x <= endX) {
            var z = startZ
            while (z <= endZ) {
                val y = world.getHighestBlockYAt(x, z)
                world.createExplosion(world.getBlockAt(x, y, z).location.add(0.5, 0.5, 0.5), 6f, false, true)
                world.getBlockAt(x, y, z).setType(Material.AIR)
                z += 8
            }
            x += 8
        }
        triggerCount = 1
    }
}
