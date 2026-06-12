package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.game.modification.GameModificationRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public final class HealthRegenListener implements Listener {

    private final ArenaManager arenaManager;

    public HealthRegenListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onHealthRegen(final EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        final me.hhitt.disasters.arena.Arena arena = arenaManager.getArena(player);
        if (arena != null && GameModificationRegistry.isActive(arena, "one-heart")) {
            event.setCancelled(true);
        }
    }
}
