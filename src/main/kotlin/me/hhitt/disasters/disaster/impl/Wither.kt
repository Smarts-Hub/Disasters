package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.EntityType
import org.bukkit.entity.Wither
import java.util.concurrent.ConcurrentHashMap

class Wither : Disaster {
    private val activeWithers = ConcurrentHashMap<Arena, Wither>()

    override fun start(arena: Arena) {
        val wither = arena.location.world.spawnEntity(arena.location, EntityType.WITHER) as Wither
        activeWithers[arena] = wither
        Notify.disaster(arena, "wither")
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
        activeWithers.remove(arena)?.remove()
    }
}