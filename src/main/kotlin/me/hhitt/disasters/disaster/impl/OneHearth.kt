package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster

class OneHearth: Disaster {
    override fun start(arena: Arena) {
        arena.playing.forEach {
            it.health = 1.0
        }
    }

    override fun pulse() {
    }

    override fun stop(arena: Arena) {
    }
}