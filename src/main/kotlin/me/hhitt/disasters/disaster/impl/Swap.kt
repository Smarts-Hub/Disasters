package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import java.util.concurrent.CopyOnWriteArrayList

class Swap : Disaster {

    val arenas = CopyOnWriteArrayList<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "swap")
    }

    override fun pulse(time: Int) {

        if(time % 10 != 0) return

        arenas.forEach {
            val players = it.alive
            players.shuffle()
            for (i in 0 until players.size - 1 step 2) {
                val player1 = players[i]
                val player2 = players[i + 1]

                val loc1 = player1.location
                val loc2 = player2.location

                player1.teleport(loc2)
                player2.teleport(loc1)
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }
}