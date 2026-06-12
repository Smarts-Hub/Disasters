package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.TNTPrimed
import org.bukkit.util.Vector
import kotlin.random.Random

class TntRain : Disaster {

    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "tnt-rain")
    }

    override fun pulse(time: Int) {
        if (time % 2 != 0) return
        arenas.toList().forEach { arena ->
            arena.alive.toList().forEach { player ->
                if (Random.nextInt(100) < 50) {
                    val target = player.location.clone().add(
                        Random.nextDouble(-12.0, 12.0), 0.0, Random.nextDouble(-12.0, 12.0)
                    )
                    if (!arena.borderService.isLocationInArena(target)) return@forEach
                    val tnt = target.world.spawn(
                        target.clone().add(0.0, Random.nextDouble(18.0, 25.0), 0.0),
                        TNTPrimed::class.java
                    )
                    tnt.fuseTicks = 60
                    tnt.yield = 4f
                    tnt.velocity = Vector(0.0, -0.5, 0.0)
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }
}
