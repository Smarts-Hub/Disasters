package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.WeatherType
import org.bukkit.block.Block
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

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
            // Break exposed top blocks across the arena area
            erodeArena(arena)

            // Damage uncovered players, and occasionally dissolve their cover
            arena.alive.toList().forEach { player ->
                val craft = player as CraftPlayer
                val block = craft.location.block

                if (isCovered(block)) {
                    // 1 in 5 chance to dissolve the block directly above the player
                    if (Random.nextInt(5) == 0) {
                        val world = block.world
                        val highestY = world.getHighestBlockYAt(block.x, block.z)
                        val topBlock = world.getBlockAt(block.x, highestY, block.z)
                        if (topBlock.type != Material.AIR && topBlock.type.isSolid) {
                            dissolveBlock(topBlock)
                        }
                    }
                } else {
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

    /**
     * Erodes blocks across the arena by picking random surface blocks
     * and dissolving them (setting to AIR with no drops).
     */
    private fun erodeArena(arena: Arena) {
        // Sample positions around all alive players to cover the arena area
        arena.alive.toList().forEach { player ->
            val loc = player.location
            val world = loc.world ?: return@forEach

            // Pick several random positions in a radius around each player
            val radius = 15
            val blocksPerPlayer = 3

            repeat(blocksPerPlayer) {
                val offsetX = Random.nextInt(-radius, radius + 1)
                val offsetZ = Random.nextInt(-radius, radius + 1)
                val x = loc.blockX + offsetX
                val z = loc.blockZ + offsetZ

                val highestY = world.getHighestBlockYAt(x, z)
                val topBlock = world.getBlockAt(x, highestY, z)

                if (topBlock.type != Material.AIR && topBlock.type.isSolid) {
                    dissolveBlock(topBlock)
                }
            }
        }
    }

    /**
     * Checks if a player's block has a solid block above them (i.e. they have cover).
     */
    private fun isCovered(block: Block): Boolean {
        val world = block.world
        val highestY = world.getHighestBlockYAt(block.x, block.z)
        return highestY > block.y
    }

    /**
     * Dissolves a block with acid effects — green particles and a slime sound.
     */
    private fun dissolveBlock(block: Block) {
        val world = block.world
        val loc = block.location.add(0.5, 0.5, 0.5)

        block.setType(Material.AIR)

        // Green drip particles
        val dust = DustOptions(Color.fromRGB(80, 200, 50), 1.2f)
        world.spawnParticle(Particle.DUST, loc, 5, 0.4, 0.4, 0.4, dust)

        // Squishy slime dissolve sound
        world.playSound(loc, Sound.BLOCK_SLIME_BLOCK_BREAK, 0.4f, 1.2f)
    }
}