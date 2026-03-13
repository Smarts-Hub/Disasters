package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Wither
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Wither : Disaster {
    private val spawnWithers = ConcurrentHashMap<Arena, Wither>()
    private val hunterWithers = ConcurrentHashMap<Arena, Wither>()
    private val lastLocations = ConcurrentHashMap<Wither, Location>()
    private val startTimes = ConcurrentHashMap<Arena, Int>()
    private val nextRetargetTimes = ConcurrentHashMap<Arena, Int>()

    override fun start(arena: Arena) {
        // Wither 1: Spawns at arena spawn, vanilla AI, no forced targeting
        val spawnWither = spawnWither(arena.location)
        spawnWithers[arena] = spawnWither

        // Wither 2: Spawns in the sky near a random player
        val target = arena.alive.randomOrNull() ?: return
        val hunterSpawn = getHunterSpawnLocation(target.location)
        val hunterWither = spawnWither(hunterSpawn)
        hunterWither.target = target
        hunterWithers[arena] = hunterWither

        Notify.disaster(arena, "wither")

        // Track when this disaster started for the 120s retarget cutoff
        startTimes[arena] = 0
        nextRetargetTimes[arena] = Random.nextInt(1, 31)
    }

    override fun pulse(time: Int) {
        // Hunter wither: break blocks every 3 seconds + random retarget interval (stops after 120s)
        hunterWithers.forEach { (arena, wither) ->
            if (!wither.isDead) {
                if (time % 3 == 0) {
                    breakBlocksAround(wither, 1)
                }

                // Increment elapsed time for this arena's wither disaster
                val elapsed = (startTimes[arena] ?: 0) + 1
                startTimes[arena] = elapsed

                // Retarget at random intervals (1-30s), stops after 120 seconds
                if (elapsed <= 120) {
                    val nextRetarget = nextRetargetTimes[arena] ?: 0
                    if (elapsed >= nextRetarget) {
                        val nearest = arena.alive.minByOrNull { it.location.distanceSquared(wither.location) }
                        if (nearest != null) {
                            wither.target = nearest
                        }
                        // Roll next random retarget interval
                        nextRetargetTimes[arena] = elapsed + Random.nextInt(1, 31)
                    }
                }
            }
        }

        // Spawn wither: vanilla AI, no forced targeting. Stuck detection + block breaking only
        spawnWithers.forEach { (_, wither) ->
            if (!wither.isDead) {
                val lastLoc = lastLocations[wither]
                val currentLoc = wither.location

                if (lastLoc != null && lastLoc.distanceSquared(currentLoc) < 1.0) {
                    breakBlocksAround(wither, 2)
                }

                lastLocations[wither] = currentLoc
            }
        }
    }

    override fun stop(arena: Arena) {
        spawnWithers.remove(arena)?.let {
            lastLocations.remove(it)
            it.remove()
        }
        hunterWithers.remove(arena)?.let {
            lastLocations.remove(it)
            it.remove()
        }
        startTimes.remove(arena)
        nextRetargetTimes.remove(arena)
    }

    /**
     * Spawns a wither at the given location.
     */
    private fun spawnWither(location: Location): Wither {
        return location.world.spawnEntity(location, EntityType.WITHER) as Wither
    }

    /**
     * Breaks solid blocks in a radius around the wither.
     * Only breaks non-air, non-liquid blocks.
     */
    private fun breakBlocksAround(wither: Wither, radius: Int) {
        val loc = wither.location
        val world = loc.world

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block: Block = world.getBlockAt(
                        loc.blockX + x,
                        loc.blockY + y,
                        loc.blockZ + z
                    )
                    if (block.type != Material.AIR && !block.isLiquid) {
                        block.type = Material.AIR
                    }
                }
            }
        }
    }

    /**
     * Gets a spawn location for the hunter wither:
     * - 30 blocks horizontal in a random direction from the player
     * - 20-30 blocks above the player's Y
     */
    private fun getHunterSpawnLocation(playerLocation: Location): Location {
        val angle = Random.nextDouble(0.0, 2 * Math.PI)
        val horizontalDistance = 30.0
        val verticalOffset = Random.nextInt(20, 31).toDouble()

        val x = playerLocation.x + cos(angle) * horizontalDistance
        val z = playerLocation.z + sin(angle) * horizontalDistance
        val y = playerLocation.y + verticalOffset

        return Location(playerLocation.world, x, y, z)
    }
}