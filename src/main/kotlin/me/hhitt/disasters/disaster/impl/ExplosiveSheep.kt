package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.model.entity.DisasterSheep
import me.hhitt.disasters.util.Notify
import net.minecraft.world.entity.Entity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.Player
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class ExplosiveSheep: Disaster {

    val arenaSheep = mutableMapOf<Arena, CopyOnWriteArrayList<DisasterSheep>>()

    override fun start(arena: Arena) {
        arenaSheep[arena] = CopyOnWriteArrayList()
        Notify.disaster(arena, "explosive-sheep")
    }

    override fun pulse(time: Int) {
        tick()

        if(time % 5 != 0) return
        arenaSheep.keys.toList().forEach { arena ->
            arena.alive.forEach { player ->
                spawnSheep(arena, player, 10, 1)
            }
        }
    }

    override fun stop(arena: Arena) {
        arenaSheep.remove(arena)?.forEach { it.remove(Entity.RemovalReason.KILLED) }
    }

    private fun tick() {
        arenaSheep.values.forEach { sheeps ->
            sheeps.removeIf { sheep ->
                if (sheep.isAlive) {
                    sheep.call()
                    false
                } else {
                    true
                }
            }
        }
    }

    private fun spawnSheep(arena: Arena, player: Player, radius: Int, amount: Int) {
        repeat(amount) {
            val spawnLocation = findSafeSpawnLocation(player.location, radius)
            spawnLocation?.let {
                val handle = (spawnLocation.world as CraftWorld).handle
                val sheep = DisasterSheep(net.minecraft.world.entity.EntityType.SHEEP, handle.level, spawnLocation)
                handle.addFreshEntity(sheep)
                arenaSheep[arena]?.add(sheep)
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
        val blockAbove = world.getBlockAt(location.clone().add(0.0, 1.0, 0.0))

        return block.type != Material.LAVA &&
                block.type != Material.CACTUS &&
                blockAbove.type == Material.AIR
    }
}