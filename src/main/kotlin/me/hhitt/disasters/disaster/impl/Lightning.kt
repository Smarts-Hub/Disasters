package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster

class Lightning : Disaster {

    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
    }

    override fun pulse() {
        arenas.forEach { arena ->
            val target = arena.alive.random()
            val location = target.location

            location.world?.strikeLightning(location)
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }
}
