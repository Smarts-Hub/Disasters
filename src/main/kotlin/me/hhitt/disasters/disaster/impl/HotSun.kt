package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import java.util.concurrent.CopyOnWriteArrayList

class HotSun : Disaster {

    private val players = CopyOnWriteArrayList<Player>()

    // 1. Get the plugin instance so we can access the config
    private val plugin = Disasters.getInstance()

    override fun start(arena: Arena) {
        arena.playing.forEach { players.add(it) }
        Notify.disaster(arena, "hot-sun")
    }

    override fun pulse(time: Int) {
        if (time % 2 != 0) return

        // 2. Read the damage amount from config (defaults to 0.5 if it can't find it)
        val damageAmount = plugin.config.getDouble("hot-sun-damage", 0.5)

        players.forEach { player ->
            val loc = player.location

            val exposed = loc.block.lightFromSky >= 15 &&
                    player.world.getHighestBlockAt(loc).y <= loc.y

            if (exposed) {
                // 3. Apply the configurable damage
                player.damage(damageAmount)
            }
        }
    }


    override fun stop(arena: Arena) {
        arena.playing.forEach { players.remove(it) }
    }
}