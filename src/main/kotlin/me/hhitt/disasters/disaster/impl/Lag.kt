package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player

class Lag : Disaster {

    private val players = mutableListOf<Player>()

    override fun start(arena: Arena) {
        arena.playing.forEach { players.add(it) }
        Notify.disaster(arena, "lag")
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach { players.remove(it) }
    }


}