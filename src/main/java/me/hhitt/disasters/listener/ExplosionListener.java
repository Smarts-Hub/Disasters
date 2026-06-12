package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class ExplosionListener implements Listener {

    private final ArenaManager arenaManager;

    public ExplosionListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        for (final Arena arena : arenaManager.getArenas()) {
            if (arena.getBorderService().isLocationInArena(event.getLocation())) {
                event.setYield(0f);
                return;
            }
        }
    }
}
