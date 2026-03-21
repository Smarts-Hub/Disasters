package me.hhitt.disasters.model.drop

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item

/**
 * Tracks a disaster item drop: the dropped item entity and its floating name ArmorStand.
 *
 * @param item The dropped item entity in the world.
 * @param label The invisible ArmorStand displaying the item name above the drop.
 * @param spawnTime The game time (seconds) when this drop was created, used for despawn tracking.
 */
class ItemDrop(
    val item: Item,
    val label: ArmorStand,
    val spawnTime: Int
) {
    fun remove() {
        if (!item.isDead) item.remove()
        if (!label.isDead) label.remove()
    }
}
