package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.World
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class Apocalypse: Disaster {

    private val arenas = CopyOnWriteArrayList<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Notify.disaster(arena, "apocalypse")
    }

    override fun pulse(time: Int) {
        if(time % 2 != 0) return
        arenas.forEach { arena ->
            arena.alive.forEach { player ->
                spawnZombiesNearPlayer(player, 10, 2)
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
    }

    private fun spawnZombiesNearPlayer(player: Player, radius: Int, amount: Int) {
        val world: World = player.world
        repeat(amount) {
            val spawnLocation = findSafeSpawnLocation(player.location, radius)
            spawnLocation?.let {
                world.spawnEntity(it, EntityType.ZOMBIE)
            }
        }
    }

    private fun findSafeSpawnLocation(location: Location, radius: Int): Location? {
        repeat(10) {
            val randomX = location.x + Random.nextDouble(-radius.toDouble(), radius.toDouble())
            val randomZ = location.z + Random.nextDouble(-radius.toDouble(), radius.toDouble())
            val highestY = location.world.getHighestBlockYAt(randomX.toInt(), randomZ.toInt()).toDouble()
            val potentialLocation = Location(location.world, randomX, highestY + 1, randomZ)

            if (isSafeLocation(potentialLocation)) {
                return potentialLocation
            }
        }
        return null
    }

    private fun isSafeLocation(location: Location): Boolean {
        val world = location.world
        val block = world.getBlockAt(location)
        val blockAbove = world.getBlockAt(location.add(0.0, 1.0, 0.0))

        return block.type != Material.LAVA &&
                block.type != Material.CACTUS &&
                blockAbove.type == Material.AIR
    }
}