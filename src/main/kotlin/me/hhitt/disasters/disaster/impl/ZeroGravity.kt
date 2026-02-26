package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import java.util.concurrent.CopyOnWriteArrayList

class ZeroGravity : Disaster {

    private val players = CopyOnWriteArrayList<Player>()
    private var count = 0

    override fun start(arena: Arena) {
        arena.playing.forEach() {
            players.add(it)
            it.addPotionEffect(
                PotionEffect(
                    org.bukkit.potion.PotionEffectType.LEVITATION,
                    20 * 5,
                    1,
                    true,
                    false
                )
            )
        }


        Notify.disaster(arena, "zero-gravity")

    }

    override fun pulse(time: Int) {
        // Runs about 60s total
        if(count > 110) return

        if (time % 11 != 0) return

        players.forEach {
            it.addPotionEffect(
                PotionEffect(
                    org.bukkit.potion.PotionEffectType.LEVITATION,
                    20 * 5,
                    1,
                    true,
                    false
                )
            )
        }

        count++
    }


    override fun stop(arena: Arena) {
        arena.playing.forEach { players.remove(it) }
    }
}