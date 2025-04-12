package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Material
import org.bukkit.WeatherType
import org.bukkit.block.Block
import org.bukkit.craftbukkit.entity.CraftPlayer

class AcidRain: Disaster {

    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arena.playing.forEach {
            val player: CraftPlayer = it as CraftPlayer
            player.handle.connection.player.setPlayerWeather(WeatherType.DOWNFALL, true)
        }
        arenas.add(arena)
        Notify.disaster(arena, "acid-rain")

    }

    override fun pulse(time: Int) {
        arenas.forEach {
            it.alive.forEach {
                val player: CraftPlayer = it as CraftPlayer
                if(!isCoveredAndBreak(player.location.block)) {
                    player.damage(1.0)
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

    private fun isCoveredAndBreak(block: Block): Boolean {
        val world = block.world
        val playerY = block.y
        var topBlock: Block? = null

        for (y in playerY + 1 until world.maxHeight) {
            val aboveBlock = world.getBlockAt(block.x, y, block.z)
            if (aboveBlock.type != Material.AIR) {
                topBlock = aboveBlock
            }
        }

        topBlock?.let {
            it.breakNaturally()
            return true
        }

        return false
    }
}