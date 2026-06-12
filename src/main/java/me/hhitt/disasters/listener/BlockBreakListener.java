package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class BlockBreakListener implements Listener {

    private final ArenaManager arenaManager;

    public BlockBreakListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (arenaManager.getArena(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }
}
