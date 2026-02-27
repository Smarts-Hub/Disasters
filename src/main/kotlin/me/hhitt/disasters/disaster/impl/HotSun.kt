package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import java.util.concurrent.CopyOnWriteArrayList

class HotSun : Disaster {

    private val players = CopyOnWriteArrayList<Player>()

    override fun start(arena: Arena) {
        arena.playing.forEach { players.add(it) }
        Notify.disaster(arena, "hot-sun")
    }

    override fun pulse(time: Int) {
        if (time % 2 != 0) return

        players.forEach { player ->
            val loc = player.location

            val exposed = loc.block.lightFromSky >= 15 &&
                    player.world.getHighestBlockAt(loc).y <= loc.y

            if (exposed) {
                player.damage(0.5)
            }
        }
    }


    override fun stop(arena: Arena) {
        arena.playing.forEach { players.remove(it) }
    }
}