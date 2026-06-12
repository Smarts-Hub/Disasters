package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.game.drop.ItemDropManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.persistence.PersistentDataType;

public final class ItemPickupListener implements Listener {

    private final ArenaManager arenaManager;

    public ItemPickupListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onItemPickup(final EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        final Arena arena = arenaManager.getArena(player);
        if (arena == null) {
            return;
        }

        if (event.getItem().getPersistentDataContainer().has(ItemDropManager.DROP_KEY, PersistentDataType.BYTE)) {
            ItemDropManager.onPickup(arena, event.getItem());
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onItemMerge(final ItemMergeEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(ItemDropManager.DROP_KEY, PersistentDataType.BYTE)
            || event.getTarget().getPersistentDataContainer().has(ItemDropManager.DROP_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }
}
