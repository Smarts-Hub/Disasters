package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.game.drop.ItemDropManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.persistence.PersistentDataType

class ItemPickupListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onItemPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val arena = arenaManager.getArena(player) ?: return

        // Allow picking up disaster drop items
        val itemEntity = event.item
        if (itemEntity.persistentDataContainer.has(ItemDropManager.DROP_KEY, PersistentDataType.BYTE)) {
            ItemDropManager.onPickup(arena, itemEntity)
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun onItemMerge(event: ItemMergeEvent) {
        // Prevent disaster drop items from merging with other items
        if (event.entity.persistentDataContainer.has(ItemDropManager.DROP_KEY, PersistentDataType.BYTE) ||
            event.target.persistentDataContainer.has(ItemDropManager.DROP_KEY, PersistentDataType.BYTE)) {
            event.isCancelled = true
        }
    }
}
