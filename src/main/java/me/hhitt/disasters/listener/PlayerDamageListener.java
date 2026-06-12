package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.game.GameState;
import me.hhitt.disasters.game.modification.GameModificationRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class PlayerDamageListener implements Listener {

    private final ArenaManager arenaManager;

    public PlayerDamageListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        final Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) {
            return;
        }
        final Player victim = (Player) event.getEntity();

        final Arena arena = arenaManager.getArena(victim);
        final Arena attackerArena = arenaManager.getArena(attacker);
        if (arena == null && attackerArena == null) {
            return;
        }

        if (arena != attackerArena) {
            event.setCancelled(true);
            return;
        }

        if (arena.getState() != GameState.LIVE || !GameModificationRegistry.isActive(arena, "pvp")) {
            event.setCancelled(true);
        }
    }

    private Player resolveAttacker(final Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            final ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }
}
