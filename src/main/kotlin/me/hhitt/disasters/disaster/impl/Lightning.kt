package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Material
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class Lightning : Disaster {

    private val arenas = CopyOnWriteArrayList<Arena>()
    private val radius = 5
    private val random = Random

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "lightning")
    }

    override fun pulse(time: Int) {
        if (time % 3 != 0) return

        arenas.forEach { arena ->
            // FIX: This prevents the "Collection is empty" error when you die
            val target = arena.alive.randomOrNull() ?: return@forEach

            val location = target.location

            val offsetX = (random.nextDouble() - 0.5) * 2 * radius
            val offsetZ = (random.nextDouble() - 0.5) * 2 * radius

            val strikeLocation = location.clone().add(offsetX, 0.0, offsetZ)

            // Safe world check
            val world = strikeLocation.world ?: return@forEach

            val highestBlockY = world.getHighestBlockYAt(strikeLocation).toDouble()
            strikeLocation.y = highestBlockY
            world.strikeLightning(strikeLocation)
            strikeLocation.block.type = Material.AIR
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }
}