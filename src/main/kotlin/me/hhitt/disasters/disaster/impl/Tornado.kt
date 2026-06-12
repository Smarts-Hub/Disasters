package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Tornado : Disaster {

    private data class State(
        val arena: Arena,
        var center: Location,
        var dirX: Double = Random.nextDouble(-1.0, 1.0),
        var dirZ: Double = Random.nextDouble(-1.0, 1.0),
        var phase: Int = 0
    )

    private val states = mutableListOf<State>()

    override fun start(arena: Arena) {
        val target = arena.alive.randomOrNull()?.location ?: arena.location
        val center = Location(
            target.world,
            target.blockX + 0.5,
            target.blockY.toDouble(),
            target.blockZ + 0.5
        )
        states.add(State(arena, center))
        Notify.disaster(arena, "tornado")
    }

    override fun pulse(time: Int) {
        states.toList().forEach { state ->
            if (time % 1 == 0) {
                state.center = state.center.clone().add(state.dirX * 1.5, 0.0, state.dirZ * 1.5)
                if (!state.arena.borderService.isLocationInArena(state.center)) {
                    state.dirX = -state.dirX
                    state.dirZ = -state.dirZ
                    state.center = state.center.clone().add(state.dirX * 1.5, 0.0, state.dirZ * 1.5)
                }
            }

            state.phase++
            spawnSpiralParticles(state)

            state.arena.alive.toList().forEach { player ->
                val dx = player.location.x - state.center.x
                val dz = player.location.z - state.center.z
                val dist = kotlin.math.sqrt(dx * dx + dz * dz)
                if (dist <= 5.0) {
                    val tangentX = -dz / dist.coerceAtLeast(0.1)
                    val tangentZ = dx / dist.coerceAtLeast(0.1)
                    val vel = Vector(tangentX * 1.4, 1.1, tangentZ * 1.4)
                    player.velocity = player.velocity.add(vel)
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        states.removeAll { it.arena == arena }
    }

    private fun spawnSpiralParticles(state: State) {
        val world = state.center.world ?: return
        for (i in 0 until 12) {
            val angle = (state.phase + i * 30) * Math.PI / 30
            val radius = 1.0 + i * 0.25
            val x = state.center.x + cos(angle) * radius
            val z = state.center.z + sin(angle) * radius
            val y = state.center.y + i * 0.5
            world.spawnParticle(Particle.CLOUD, x, y, z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }
}
