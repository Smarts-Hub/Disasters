package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.storage.data.Data;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerLeaveListener implements Listener {

    private final ArenaManager arenaManager;

    public PlayerLeaveListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onPlayerLeave(final PlayerQuitEvent event) {
        Data.unloadPlayerFromCache(event.getPlayer().getUniqueId());

        final Arena arena = arenaManager.getArena(event.getPlayer());
        if (arena != null) {
            arena.removePlayer(event.getPlayer());
        }
    }
}
