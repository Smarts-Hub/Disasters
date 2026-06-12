package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Lightning : Disaster, TriggerTrackedDisaster {

    override var triggerCount: Int = 0
        private set

    private val arenas = CopyOnWriteArrayList<Arena>()
    private val radius = 5
    private val tasks = mutableListOf<BukkitTask>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "lightning")
    }

    override fun pulse(time: Int) {
        if (time % 3 != 0) return
        for (arena in arenas) {
            val target = arena.alive.randomOrNull() ?: continue
            val playerLocation = target.location
            val offsetX = (Random.nextDouble() - 0.5) * 2 * radius
            val offsetZ = (Random.nextDouble() - 0.5) * 2 * radius
            val strikeLocation = playerLocation.clone().add(offsetX, 0.0, offsetZ)
            val world = strikeLocation.world ?: continue
            val highestBlockY = world.getHighestBlockYAt(strikeLocation).toDouble()
            strikeLocation.y = highestBlockY

            for (i in 0 until 12) {
                val angle = i * (Math.PI * 2 / 12)
                val px = strikeLocation.x + cos(angle) * 1.5
                val pz = strikeLocation.z + sin(angle) * 1.5
                world.spawnParticle(
                    Particle.DUST,
                    px, strikeLocation.y + 0.5, pz,
                    2, 0.1, 0.0, 0.1,
                    Particle.DustOptions(Color.YELLOW, 1.5f)
                )
            }
            world.playSound(strikeLocation, Sound.ENTITY_BEE_LOOP, 1f, 1.5f)

            val task = Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), Runnable {
                for (player in arena.alive) {
                    if (player.location.distanceSquared(strikeLocation) <= 16.0) {
                        DeathMessageService.mark(player, "lightning")
                    }
                }
                world.strikeLightning(strikeLocation)
                world.createExplosion(strikeLocation, 2f, false, true)
                triggerCount++
            }, 20L)
            tasks.add(task)
        }
    }

    override fun stop(arena: Arena) {
        tasks.forEach { it.cancel() }
        tasks.clear()
        arenas.remove(arena)
    }
}
