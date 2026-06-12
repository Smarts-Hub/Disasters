package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import me.hhitt.disasters.util.SpawnLocationFinder
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class Apocalypse : Disaster {

    private enum class ZombieVariant(val weight: Int) {
        NORMAL(55), SPEED(20), BOOMER(10), GIANT(5), JUMPER(10)
    }

    private val arenas = CopyOnWriteArrayList<Arena>()
    private val lastLocations = ConcurrentHashMap<Arena, ConcurrentHashMap<Zombie, org.bukkit.Location>>()
    private val boomers = ConcurrentHashMap<Arena, MutableSet<UUID>>()
    private val jumpers = ConcurrentHashMap<Arena, MutableSet<UUID>>()
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
        boomers[arena] = mutableSetOf()
        jumpers[arena] = mutableSetOf()
        Notify.disaster(arena, "apocalypse")
    }

    override fun pulse(time: Int) {
        arenas.forEach { arena ->
            val elapsed = (startTimes[arena] ?: 0) + 1
            startTimes[arena] = elapsed

            val shouldSpawn = when {
                elapsed <= 20 -> elapsed % 2 == 0
                elapsed <= 120 -> elapsed % 5 == 0
                else -> elapsed % 60 == 0
            }

            if (shouldSpawn) {
                val zombieMap = lastLocations[arena] ?: return@forEach
                val aliveCount = zombieMap.keys.count { !it.isDead }
                if (aliveCount < MAX_ALIVE) {
                    arena.alive.forEach { player ->
                        spawnZombiesNearPlayer(arena, player, 2)
                    }
                }
            }

            // Jumper behavior
            jumpers[arena]?.toList()?.forEach { id ->
                val zombie = org.bukkit.Bukkit.getEntity(id) as? Zombie
                if (zombie == null || zombie.isDead) {
                    jumpers[arena]?.remove(id)
                    return@forEach
                }
                val target = arena.alive.minByOrNull { it.location.distanceSquared(zombie.location) } ?: return@forEach
                val dx = target.location.x - zombie.location.x
                val dz = target.location.z - zombie.location.z
                val dist = kotlin.math.sqrt(dx * dx + dz * dz)
                if (dist < 12.0 && elapsed % 5 == 0) {
                    val vel = zombie.velocity
                    vel.x = (dx / dist) * 0.4
                    vel.z = (dz / dist) * 0.4
                    vel.y = 1.0
                    zombie.velocity = vel
                }
            }

            // Boomer cleanup: if a boomer died, mark nearby players and explode
            val boomerSet = boomers[arena] ?: return@forEach
            boomerSet.toList().forEach { id ->
                val zombie = org.bukkit.Bukkit.getEntity(id) as? Zombie
                if (zombie == null || zombie.isDead) {
                    val loc = zombie?.location
                    if (loc != null) {
                        arena.alive.toList().forEach { p ->
                            if (p.location.distanceSquared(loc) <= 16.0) {
                                DeathMessageService.mark(p, "apocalypse")
                            }
                        }
                        loc.world.createExplosion(loc, 2f, false, true)
                    }
                    boomerSet.remove(id)
                }
            }

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
                        val nearestPlayer = arena.alive.minByOrNull {
                            it.location.distanceSquared(zombie.location)
                        }
                        if (nearestPlayer != null) {
                            breakBlocksTowardTarget(zombie, nearestPlayer.location)
                        }
                    }
                    zombieMap[zombie] = currentLoc
                }
                deadZombies.forEach { zombieMap.remove(it) }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        startTimes.remove(arena)
        boomers.remove(arena)
        jumpers.remove(arena)
        lastLocations.remove(arena)?.keys?.forEach { zombie ->
            if (!zombie.isDead) zombie.remove()
        }
    }

    private fun spawnZombiesNearPlayer(arena: Arena, player: Player, amount: Int) {
        val world = player.world
        val zombieMap = lastLocations[arena] ?: return
        val boomerSet = boomers[arena] ?: return
        val jumperSet = jumpers[arena] ?: return
        repeat(amount) {
            val spawnLocation = SpawnLocationFinder.findNearPlayer(arena, player.location, MIN_SPAWN_DISTANCE, SPAWN_RADIUS, Y_VARIATION)
            val zombie = world.spawnEntity(spawnLocation, EntityType.ZOMBIE) as Zombie

            val variant = pickVariant()
            when (variant) {
                ZombieVariant.NORMAL -> {
                    if (Random.nextInt(20) == 0) zombie.setBaby()
                    if (Random.nextBoolean()) zombie.equipment?.helmet = ItemStack(Material.LEATHER_HELMET)
                }
                ZombieVariant.SPEED -> {
                    if (Random.nextInt(20) == 0) zombie.setBaby()
                    zombie.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = (zombie.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue ?: 0.23) * 1.6
                    zombie.equipment?.helmet = ItemStack(Material.LEATHER_HELMET)
                }
                ZombieVariant.BOOMER -> {
                    zombie.equipment?.helmet = ItemStack(Material.TNT)
                    boomerSet.add(zombie.uniqueId)
                }
                ZombieVariant.GIANT -> {
                    val scaleAttr = zombie.getAttribute(Attribute.SCALE)
                    if (scaleAttr != null) {
                        scaleAttr.baseValue = 2.0
                    }
                }
                ZombieVariant.JUMPER -> {
                    jumperSet.add(zombie.uniqueId)
                }
            }

            zombieMap[zombie] = zombie.location
        }
    }

    private fun pickVariant(): ZombieVariant {
        val total = ZombieVariant.values().sumOf { it.weight }
        var roll = Random.nextInt(total)
        for (v in ZombieVariant.values()) {
            roll -= v.weight
            if (roll < 0) return v
        }
        return ZombieVariant.NORMAL
    }

    private fun breakBlocksTowardTarget(zombie: Zombie, target: org.bukkit.Location) {
        val zombieLoc = zombie.location
        val direction = target.toVector().subtract(zombieLoc.toVector()).normalize()

        for (dist in 1..2) {
            val checkX = (zombieLoc.x + direction.x * dist).toInt()
            val checkZ = (zombieLoc.z + direction.z * dist).toInt()
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
