package me.hhitt.disasters.arena.service

import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

/**
 * BorderService handles the arena borders.
 * It checks if a player is inside the arena and teleports them if they are outside.
 *
 * @param corner1 The first corner of the arena.
 * @param corner2 The second corner of the arena.
 */

class BorderService(
    private val corner1: Location,
    private val corner2: Location
) {

    private val x1 = corner1.x
    private val y1 = corner1.y
    private val z1 = corner1.z
    private val x2 = corner2.x
    private val y2 = corner2.y
    private val z2 = corner2.z

    private val minX = min(x1, x2).toInt()
    private val maxX = max(x1, x2).toInt()
    private val minZ = min(z1, z2).toInt()
    private val maxZ = max(z1, z2).toInt()
    private val maxY = max(y1, y2).toInt()

    fun isLocationInArena(loc: Location): Boolean {
        val world = loc.world ?: return false
        val arenaWorld = corner1.world ?: return false

        if (!world.name.equals(arenaWorld.name, ignoreCase = true)) {
            return false
        }

        return loc.x >= minX && loc.x <= maxX && loc.y <= maxY && loc.z >= minZ && loc.z <= maxZ
    }

    fun isLocationInArenaTp(player: Player): Boolean {
        val currentLoc = player.location
        val world = currentLoc.world ?: return false
        val arenaWorld = corner1.world ?: return false

        if (!world.name.equals(arenaWorld.name, ignoreCase = true)) {
            return false
        }

        var newX = currentLoc.x
        var newY = currentLoc.y
        var newZ = currentLoc.z

        if (newX < minX) {
            newX = minX + 0.5
        } else if (newX > maxX) {
            newX = maxX - 0.5
        }

        if (newZ < minZ) {
            newZ = minZ + 0.5
        } else if (newZ > maxZ) {
            newZ = maxZ - 0.5
        }

        if (newY > maxY) {
            newY = maxY.toDouble()
        }

        if (newX != currentLoc.x || newY != currentLoc.y || newZ != currentLoc.z) {
            val newLocation = Location(currentLoc.world, newX, newY, newZ, currentLoc.yaw, currentLoc.pitch)
            player.teleport(newLocation)
        }

        return newX >= minX && newX <= maxX && newY <= maxY && newZ >= minZ && newZ <= maxZ
    }

}