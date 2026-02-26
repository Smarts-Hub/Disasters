package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Material
import org.bukkit.WeatherType
import org.bukkit.block.Block
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.util.concurrent.CopyOnWriteArrayList

class AcidRain: Disaster {

    private val arenas = CopyOnWriteArrayList<Arena>()

    override fun start(arena: Arena) {
        arena.playing.forEach {
            val player: CraftPlayer = it as CraftPlayer
            player.handle.connection.player.setPlayerWeather(WeatherType.DOWNFALL, true)
        }
        arenas.add(arena)
        Notify.disaster(arena, "acid-rain")

    }

    override fun pulse(time: Int) {
        arenas.forEach { arena ->
            arena.alive.toList().forEach { player ->
                val craft = player as CraftPlayer

                if (!isCoveredAndBreakFast(craft.location.block)) {
                    craft.damage(2.0)
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach {
            val player: CraftPlayer = it as CraftPlayer
            player.handle.connection.player.setPlayerWeather(WeatherType.CLEAR, true)
        }
        arenas.remove(arena)
    }

    private fun isCoveredAndBreakFast(block: Block): Boolean {
        val world = block.world

        val highestY = world.getHighestBlockYAt(block.x, block.z)

        if (highestY > block.y) {
            val topBlock = world.getBlockAt(block.x, highestY, block.z)

            if (topBlock.type != Material.AIR) {
                topBlock.breakNaturally()
                return true
            }
        }

        return false
    }
}