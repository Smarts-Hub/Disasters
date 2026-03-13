package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class Apocalypse : Disaster {

    private val arenas = CopyOnWriteArrayList<Arena>()
    private val lastLocations = ConcurrentHashMap<Arena, ConcurrentHashMap<Zombie, Location>>()
    private val startTimes = ConcurrentHashMap<Arena, Int>()

    companion object {
        private const val MAX_ALIVE = 100
        private const val MIN_SPAWN_DISTANCE = 10
        private const val SPAWN_RADIUS = 15
        private const val STUCK_CHECK_INTERVAL = 5
        private const val Y_VARIATION = 5
    }

    override fun start(arena: Arena) {
        arenas.add(arena)
        startTimes[arena] = 0
        lastLocations[arena] = ConcurrentHashMap()
        Notify.disaster(arena, "apocalypse")
    }

    override fun pulse(time: Int) {
        arenas.forEach { arena ->
            val elapsed = (startTimes[arena] ?: 0) + 1
            startTimes[arena] = elapsed

            // Spawn rate ramp: fast early, slows down over time
            val shouldSpawn = when {
                elapsed <= 20 -> elapsed % 2 == 0       // First 20s: 2 per player every 2 seconds
                elapsed <= 120 -> elapsed % 5 == 0      // 20-120s: 2 per player every 5 seconds
                else -> elapsed % 60 == 0               // After 120s: 2 per player every 60 seconds
            }

            if (shouldSpawn) {
                // Check alive zombie count before spawning
                val zombieMap = lastLocations[arena] ?: return@forEach
                val aliveCount = zombieMap.keys.count { !it.isDead }
                if (aliveCount < MAX_ALIVE) {
                    arena.alive.forEach { player ->
                        spawnZombiesNearPlayer(arena, player, 2)
                    }
                }
            }

            // Stuck detection every 5 seconds — directional block breaking toward nearest player
            if (time % STUCK_CHECK_INTERVAL == 0) {
                val zombieMap = lastLocations[arena] ?: return@forEach
                val deadZombies = mutableListOf<Zombie>()

                zombieMap.forEach { (zombie, lastLoc) ->
                    if (zombie.isDead) {
                        deadZombies.add(zombie)
                        return@forEach
                    }

                    val currentLoc = zombie.location

                    if (lastLoc.distanceSquared(currentLoc) < 1.0) {
                        // Find nearest player to break toward
                        val nearestPlayer = arena.alive.minByOrNull {
                            it.location.distanceSquared(zombie.location)
                        }
                        if (nearestPlayer != null) {
                            breakBlocksTowardTarget(zombie, nearestPlayer.location)
                        }
                    }

                    zombieMap[zombie] = currentLoc
                }

                // Clean up dead zombies from tracking
                deadZombies.forEach { zombieMap.remove(it) }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        startTimes.remove(arena)

        // Kill all zombies in the arena and clean up tracking
        lastLocations.remove(arena)?.keys?.forEach { zombie ->
            if (!zombie.isDead) {
                zombie.remove()
            }
        }
    }

    private fun spawnZombiesNearPlayer(arena: Arena, player: Player, amount: Int) {
        val world = player.world
        val zombieMap = lastLocations[arena] ?: return
        repeat(amount) {
            val spawnLocation = findSafeSpawnLocation(player.location)
            spawnLocation?.let {
                val zombie = world.spawnEntity(it, EntityType.ZOMBIE) as Zombie

                // 1 in 20 chance for baby zombie
                if (Random.nextInt(20) == 0) {
                    zombie.setBaby()
                }

                // 50% chance for leather helmet (prevents burning in sunlight)
                if (Random.nextBoolean()) {
                    zombie.equipment?.helmet = ItemStack(Material.LEATHER_HELMET)
                }

                // Track for stuck detection
                zombieMap[zombie] = zombie.location
            }
        }
    }

    private fun findSafeSpawnLocation(playerLocation: Location): Location? {
        repeat(10) {
            // Random angle for direction
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            // Random distance between min spawn distance and spawn radius
            val distance = Random.nextDouble(MIN_SPAWN_DISTANCE.toDouble(), SPAWN_RADIUS.toDouble())

            val randomX = playerLocation.x + Math.cos(angle) * distance
            val randomZ = playerLocation.z + Math.sin(angle) * distance
            // Random Y variation around player's Y level
            val randomY = playerLocation.y + Random.nextInt(-Y_VARIATION, Y_VARIATION + 1)

            // Find the nearest solid ground at or below the random Y
            val groundY = findGroundY(playerLocation.world, randomX.toInt(), randomY.toInt(), randomZ.toInt())
                ?: return@repeat

            val potentialLocation = Location(playerLocation.world, randomX, groundY + 1.0, randomZ)

            if (isSafeLocation(potentialLocation)) {
                return potentialLocation
            }
        }
        return null
    }

    /**
     * Finds the nearest solid ground at or below the given Y.
     * Returns null if no solid ground found within a reasonable range.
     */
    private fun findGroundY(world: org.bukkit.World, x: Int, startY: Int, z: Int): Double? {
        for (y in startY downTo (startY - 10)) {
            val block = world.getBlockAt(x, y, z)
            val blockAbove = world.getBlockAt(x, y + 1, z)
            if (block.type.isSolid && blockAbove.type == Material.AIR) {
                return y.toDouble()
            }
        }
        return null
    }

    private fun isSafeLocation(location: Location): Boolean {
        val world = location.world
        val block = world.getBlockAt(location)
        val blockBelow = world.getBlockAt(location.clone().add(0.0, -1.0, 0.0))
        val blockAbove = world.getBlockAt(location.clone().add(0.0, 1.0, 0.0))

        return block.type == Material.AIR &&
                blockAbove.type == Material.AIR &&
                blockBelow.type.isSolid &&
                blockBelow.type != Material.LAVA &&
                blockBelow.type != Material.CACTUS
    }

    /**
     * Breaks blocks in the direction toward a target location.
     * Only breaks blocks at body and head height in front of the zombie, never below.
     */
    private fun breakBlocksTowardTarget(zombie: Zombie, target: Location) {
        val zombieLoc = zombie.location
        val direction = target.toVector().subtract(zombieLoc.toVector()).normalize()

        // Check 1-2 blocks ahead in the direction of the target at body and head height
        for (dist in 1..2) {
            val checkX = (zombieLoc.x + direction.x * dist).toInt()
            val checkZ = (zombieLoc.z + direction.z * dist).toInt()

            // Body height and head height
            for (yOffset in 0..1) {
                val checkY = zombieLoc.blockY + yOffset
                val block: Block = zombieLoc.world.getBlockAt(checkX, checkY, checkZ)

                if (block.type != Material.AIR && !block.isLiquid) {
                    block.type = Material.AIR
                }
            }
        }
    }
}