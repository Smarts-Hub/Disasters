package me.hhitt.disasters.util

import me.hhitt.disasters.arena.Arena
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World

object SpawnLocationFinder {

    private val directions = listOf(
        1 to 0, -1 to 0, 0 to 1, 0 to -1,
        1 to 1, 1 to -1, -1 to 1, -1 to -1
    )

    fun findNearPlayer(arena: Arena, origin: Location, minDistance: Int, maxRadius: Int, verticalSearch: Int): Location {
        findSameY(arena, origin, minDistance, maxRadius)?.let { return it }
        findAroundXZWithVerticalSearch(arena, origin, minDistance, maxRadius, verticalSearch)?.let { return it }
        findAdjacent(arena, origin)?.let { return it }
        return origin.clone().add(1.0, 0.0, 0.0)
    }

    private fun findSameY(arena: Arena, origin: Location, minDistance: Int, maxRadius: Int): Location? {
        val world = origin.world ?: return null
        for (distance in minDistance..maxRadius) {
            for ((dx, dz) in directions) {
                val loc = Location(
                    world,
                    origin.blockX + dx * distance + 0.5,
                    origin.blockY.toDouble(),
                    origin.blockZ + dz * distance + 0.5
                )
                if (isSafe(arena, loc)) return loc
            }
        }
        return null
    }

    private fun findAroundXZWithVerticalSearch(arena: Arena, origin: Location, minDistance: Int, maxRadius: Int, verticalSearch: Int): Location? {
        val world = origin.world ?: return null
        for (distance in minDistance..maxRadius) {
            for ((dx, dz) in directions) {
                val x = origin.blockX + dx * distance
                val z = origin.blockZ + dz * distance
                findGroundNearY(world, x, z, origin.blockY, verticalSearch)?.let { y ->
                    val loc = Location(world, x + 0.5, y + 1.0, z + 0.5)
                    if (isSafe(arena, loc)) return loc
                }
            }
        }
        return null
    }

    private fun findAdjacent(arena: Arena, origin: Location): Location? {
        val world = origin.world ?: return null
        for ((dx, dz) in directions) {
            val loc = Location(
                world,
                origin.blockX + dx + 0.5,
                origin.blockY.toDouble(),
                origin.blockZ + dz + 0.5
            )
            if (isSafe(arena, loc)) return loc
        }
        return null
    }

    private fun findGroundNearY(world: World, x: Int, z: Int, startY: Int, verticalSearch: Int): Int? {
        for (offset in 0..verticalSearch) {
            val down = startY - offset
            if (hasGround(world, x, down, z)) return down
            val up = startY + offset
            if (offset != 0 && hasGround(world, x, up, z)) return up
        }
        return null
    }

    private fun hasGround(world: World, x: Int, y: Int, z: Int): Boolean {
        val ground = world.getBlockAt(x, y - 1, z)
        val feet = world.getBlockAt(x, y, z)
        val head = world.getBlockAt(x, y + 1, z)
        return ground.type.isSolid && ground.type != Material.LAVA && ground.type != Material.CACTUS && feet.type.isAir && head.type.isAir
    }

    fun isSafe(arena: Arena, location: Location): Boolean {
        if (!arena.borderService.isLocationInArena(location)) return false
        val world = location.world ?: return false
        return hasGround(world, location.blockX, location.blockY, location.blockZ)
    }
}
