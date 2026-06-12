package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PlayerDeathListener implements Listener {

    private final ArenaManager arenaManager;

    public PlayerDeathListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final org.bukkit.entity.Player player = event.getPlayer();
        final Arena arena = arenaManager.getArena(player);
        if (arena == null) {
            return;
        }
        arena.playerDied(player);
        event.deathMessage(DeathMessageService.messageFor(player));
        DeathMessageService.clear(player);
        Notify.playerDied(player, arena);
    }
}
