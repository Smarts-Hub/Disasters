package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify

// TODO: Implement the Hot Potato disaster

class HotPotato : Disaster {

    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "hot-potato")
    }

    override fun pulse(time: Int) {
        TODO("Not yet implemented")
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }

}