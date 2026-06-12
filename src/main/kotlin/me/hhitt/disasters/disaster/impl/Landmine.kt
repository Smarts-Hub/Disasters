package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class Landmine : Disaster, TriggerTrackedDisaster, Listener {

    override var triggerCount: Int = 0
        private set

    private data class MineKey(val world: String, val x: Int, val y: Int, val z: Int)

    private val mines = ConcurrentHashMap<Arena, MutableSet<MineKey>>()
    private val tasks = mutableListOf<BukkitTask>()
    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        mines[arena] = mutableSetOf()
        Bukkit.getPluginManager().registerEvents(this, me.hhitt.disasters.Disasters.getInstance())
        Notify.disaster(arena, "landmine")
    }

    override fun pulse(time: Int) {
        if (time % 5 != 0) return
        arenas.toList().forEach { arena ->
            val set = mines[arena] ?: return@forEach
            arena.alive.toList().forEach { player ->
                val origin = player.location
                for (i in 0 until 2) {
                    val dx = Random.nextInt(-5, 6)
                    val dz = Random.nextInt(-5, 6)
                    val x = origin.blockX + dx
                    val z = origin.blockZ + dz
                    val world = origin.world
                    val y = world.getHighestBlockYAt(x, z)
                    val key = MineKey(world.name, x, y, z)
                    if (arena.borderService.isLocationInArena(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
                        set.add(key)
                    }
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        tasks.forEach { it.cancel() }
        tasks.clear()
        mines.remove(arena)
        arenas.remove(arena)
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val arena = arenas.firstOrNull { it.isPlayerValid(event.player) } ?: return
        if (event.from.block == event.to.block) return
        val set = mines[arena] ?: return
        val key = MineKey(event.to.world.name, event.to.blockX, event.to.blockY - 1, event.to.blockZ)
        if (set.remove(key)) {
            val center = event.to.clone().subtract(0.0, 1.0, 0.0).add(0.5, 0.5, 0.5)
            event.player.world.playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
            val plugin = me.hhitt.disasters.Disasters.getInstance()
            val task = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                event.player.world.createExplosion(center, 4f, false, true)
                arena.alive.toList().forEach { p ->
                    if (p.location.distance(center) <= 5.0) {
                        DeathMessageService.mark(p, "landmine")
                    }
                }
                triggerCount++
            }, 20L)
            tasks.add(task)
        }
    }
}
