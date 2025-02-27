package me.hhitt.disasters.arena.services

import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

class Border(
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

        if (!loc.world.name.equals(corner1.world.name, ignoreCase = true)) {
            return false
        }

        return loc.x >= minX && loc.x <= maxX && loc.y <= maxY && loc.z >= minZ && loc.z <= maxZ
    }

    fun isLocationInArenaTp(player: Player): Boolean {

        if (!player.location.world.name.equals(corner1.world.name, ignoreCase = true)) {
            return false
        }

        val loc = player.location

        if (loc.x < minX) {
            loc.x = minX + 0.5
        } else if (loc.x > maxX) {
            loc.x = maxX - 0.5
        }

        if (loc.z < minZ) {
            loc.z = minZ + 0.5
        } else if (loc.z > maxZ) {
            loc.z = maxZ - 0.5
        }

        if (loc.y > maxY) {
            loc.y = maxY.toDouble()
        }

        if (loc.x != player.location.x || loc.z != player.location.z || loc.y != player.location.y) {
            player.teleport(loc)
        }

        return loc.x >= minX && loc.x <= maxX && loc.y <= maxY && loc.z >= minZ && loc.z <= maxZ
    }

}