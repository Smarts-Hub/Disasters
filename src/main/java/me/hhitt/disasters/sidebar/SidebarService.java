package me.hhitt.disasters.sidebar;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.storage.file.FileManager;

public class SidebarService {

    private final ArenaManager arenaManager;
    private final SidebarManager sidebarManager;
    private SidebarTask sidebarTask;

    public SidebarService(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
        this.sidebarManager = new SidebarManager();
        updateSidebar();
    }

    public void updateSidebar() {
        final boolean isScoreboardEnabled = FileManager.get("config").getBoolean("enable-scoreboard");

        if (isScoreboardEnabled) {
            if (sidebarTask == null || sidebarTask.isCancelled()) {
                sidebarTask = new SidebarTask(arenaManager, sidebarManager);
                sidebarTask.runTaskTimer(Disasters.getInstance(), 0, 20L);
            }
        } else {
            if (sidebarTask != null) {
                sidebarTask.cancel();
                sidebarTask = null;
            }
        }
    }

    public void shutdown() {
        if (sidebarTask != null) {
            sidebarTask.cancel();
            sidebarTask = null;
        }
        sidebarManager.shutdown();
    }
}
