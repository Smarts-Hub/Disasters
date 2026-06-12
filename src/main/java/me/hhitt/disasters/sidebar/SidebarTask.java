package me.hhitt.disasters.sidebar;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SidebarTask extends BukkitRunnable {

    private final ArenaManager arenaManager;
    private final SidebarManager sidebarManager;

    public SidebarTask(final ArenaManager arenaManager, final SidebarManager sidebarManager) {
        this.arenaManager = arenaManager;
        this.sidebarManager = sidebarManager;
    }

    @Override
    public void run() {
        sidebarManager.cleanupOfflinePlayers();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final Arena arena = arenaManager.getArena(player);
            final GameState state = arena != null ? arena.getState() : null;
            sidebarManager.updateSidebar(player, state, arena);
        }
    }
}
