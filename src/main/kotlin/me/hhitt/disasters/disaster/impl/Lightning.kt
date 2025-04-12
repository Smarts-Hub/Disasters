package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import kotlin.random.Random

class Lightning : Disaster {

    private val arenas = mutableListOf<Arena>()
    private val radius = 5
    private val random = Random

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "lightning")
    }

    override fun pulse(time: Int) {
        if (time % 3 != 0) return
        arenas.forEach { arena ->
            val target = arena.alive.random()
            val location = target.location

            val offsetX = (random.nextDouble() - 0.5) * 2 * radius
            val offsetZ = (random.nextDouble() - 0.5) * 2 * radius

            val strikeLocation = location.clone().add(offsetX, 0.0, offsetZ)
            val highestBlockY = strikeLocation.world?.getHighestBlockYAt(strikeLocation)?.toDouble() ?: strikeLocation.y
            strikeLocation.y = highestBlockY
            strikeLocation.world?.strikeLightning(strikeLocation)
        }
    }


    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }
}
