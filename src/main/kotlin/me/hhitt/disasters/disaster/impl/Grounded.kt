package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import java.util.concurrent.CopyOnWriteArrayList

class Grounded: Disaster {

    private val players = CopyOnWriteArrayList<Player>()

    override fun start(arena: Arena) {
        arena.playing.forEach { players.add(it) }
        Notify.disaster(arena, "grounded")
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach { players.remove(it) }
    }

    fun isGrounded(player: Player): Boolean {
        return players.contains(player)
    }

}