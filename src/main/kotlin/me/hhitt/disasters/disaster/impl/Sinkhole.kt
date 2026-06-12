package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

class Sinkhole : Disaster, TriggerTrackedDisaster, Listener {

    override var triggerCount: Int = 0
        private set

    private val arenas = mutableListOf<Arena>()
    private val tasks = mutableListOf<BukkitTask>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Bukkit.getPluginManager().registerEvents(this, me.hhitt.disasters.Disasters.getInstance())
        Notify.disaster(arena, "sinkhole")
    }

    override fun pulse(time: Int) {
        if (time % 4 != 0) return
        arenas.toList().forEach { arena ->
            val target = arena.alive.randomOrNull() ?: return@forEach
            val center = target.location.block
            val world = center.world
            val radius = 2
            warnAndCollapse(arena, world, center.x, center.y, center.z, radius)
        }
    }

    override fun stop(arena: Arena) {
        tasks.forEach { it.cancel() }
        tasks.clear()
        arenas.remove(arena)
        HandlerList.unregisterAll(this)
    }

    private fun warnAndCollapse(arena: Arena, world: org.bukkit.World, cx: Int, cy: Int, cz: Int, radius: Int) {
        val positions = mutableListOf<Triple<Int, Int, Int>>()
        for (dx in -radius..radius) for (dz in -radius..radius) positions.add(Triple(cx + dx, cy, cz + dz))

        val plugin = me.hhitt.disasters.Disasters.getInstance()
        val t1 = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            for ((x, y, z) in positions) world.getBlockAt(x, y, z).setType(Material.YELLOW_CONCRETE)
        }, 10L)
        val t2 = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            for ((x, y, z) in positions) world.getBlockAt(x, y, z).setType(Material.ORANGE_CONCRETE)
        }, 20L)
        val t3 = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            for ((x, y, z) in positions) world.getBlockAt(x, y, z).setType(Material.RED_CONCRETE)
        }, 30L)
        val t4 = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            for ((x, y, z) in positions) {
                world.getBlockAt(x, y, z).setType(Material.AIR)
                if (y - 5 > 0) world.getBlockAt(x, y - 5, z).setType(Material.LAVA)
            }
            arena.alive.toList().forEach { player ->
                if (kotlin.math.abs(player.location.blockX - cx) <= radius &&
                    kotlin.math.abs(player.location.blockZ - cz) <= radius &&
                    kotlin.math.abs(player.location.blockY - cy) <= 2
                ) {
                    DeathMessageService.mark(player, "sinkhole")
                    player.damage(8.0)
                }
            }
            triggerCount++
        }, 30L)
        tasks.add(t1); tasks.add(t2); tasks.add(t3); tasks.add(t4)
    }
}
