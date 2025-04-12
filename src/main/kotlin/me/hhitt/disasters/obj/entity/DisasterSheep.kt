package me.hhitt.disasters.obj.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Sheep
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.Level
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.TNTPrimed

/**
 * DisasterSheep is a class that represents a sheep entity that changes color and explodes
 * in a sequence of colors: green, orange, and red. After the red color, it spawns a TNT explosion.
 *
 * @param entityType The type of the entity (Sheep).
 * @param level The level where the entity is spawned.
 * @param location The location where the entity is spawned.
 */

class DisasterSheep(entityType: EntityType<out Sheep>, level: Level, location: Location): Sheep(entityType, level) {

    private var tick: Int = 3

    init {
        this.isInvulnerable = true
        this.color = DyeColor.GREEN
        this.setPos(location.x, location.y, location.z)
        this.isNoAi = true
    }

    fun call() {

        when(tick) {
            3 -> {
                this.color = DyeColor.GREEN
                tick--
            }
            2 -> {
                this.color = DyeColor.ORANGE
                tick--
            }
            1 -> {
                this.color = DyeColor.RED
                tick--
            }
            0 -> {
                val location = Location(this.level().world , this.x, this.y, this.z)
                spawnTNTExplosion(location)
                this.remove(RemovalReason.KILLED)
            }
        }
    }


    private fun spawnTNTExplosion(location: Location) {
        val world: World = location.world ?: return

        val tnt = world.spawnEntity(location, org.bukkit.entity.EntityType.TNT) as TNTPrimed
        tnt.fuseTicks = 0
        tnt.isSilent = false
        tnt.yield = 4.0f
    }

}