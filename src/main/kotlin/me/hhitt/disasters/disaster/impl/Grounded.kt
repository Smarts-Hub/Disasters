package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import org.bukkit.entity.Player

class Grounded: Disaster {

    val players = mutableListOf<Player>()

    override fun start(arena: Arena) {
        arena.playing.forEach { players.add(it) }
    }

    override fun pulse() {
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach { players.remove(it) }
    }

    fun isGrounded(player: Player): Boolean {
        return players.contains(player)
    }

}