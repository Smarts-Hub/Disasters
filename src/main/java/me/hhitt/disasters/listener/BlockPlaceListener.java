package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockPlaceListener implements Listener {

    private final ArenaManager arenaManager;

    public BlockPlaceListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (arenaManager.getArena(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }
}
