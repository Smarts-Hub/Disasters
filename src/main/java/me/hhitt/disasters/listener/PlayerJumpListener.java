package me.hhitt.disasters.listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.disaster.DisasterRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class PlayerJumpListener implements Listener {

    private final ArenaManager arenaManager;

    public PlayerJumpListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onPlayerJump(final PlayerJumpEvent event) {
        final org.bukkit.entity.Player player = event.getPlayer();
        final Arena arena = arenaManager.getArena(player);
        if (arena != null && DisasterRegistry.isGrounded(arena, player)) {
            event.setCancelled(true);
        }
    }
}
