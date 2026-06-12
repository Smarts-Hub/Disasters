package me.hhitt.disasters.listener;

import me.hhitt.disasters.storage.data.Data;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final java.util.UUID playerId = event.getPlayer().getUniqueId();
        Data.loadPlayer(playerId);
    }
}
