package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class PlayerDamageListener implements Listener {

    private final ArenaManager arenaManager;

    public PlayerDamageListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        final Player victim = (Player) event.getEntity();
        final Player attacker = (Player) event.getDamager();

        final Arena arena = arenaManager.getArena(victim);
        if (arena == null) {
            return;
        }
        final Arena attackerArena = arenaManager.getArena(attacker);

        if (arena != attackerArena) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
    }
}
