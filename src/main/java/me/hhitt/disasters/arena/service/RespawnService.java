package me.hhitt.disasters.arena.service;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RespawnService {

    private final Arena arena;
    private final JavaPlugin plugin;

    public RespawnService(final Arena arena) {
        this.arena = arena;
        this.plugin = Disasters.getInstance();
    }

    public RespawnService(final Arena arena, final JavaPlugin plugin) {
        this.arena = arena;
        this.plugin = plugin;
    }

    public void setSpectator(final Player player) {
        respawnThen(player, new Runnable() {
            @Override
            public void run() {
                player.setGameMode(GameMode.SPECTATOR);
                player.setHealth(20.0);
                player.teleport(getSpectatorLocation());
            }
        });
    }

    public void respawnAtArena(final Player player) {
        respawnThen(player, new Runnable() {
            @Override
            public void run() {
                player.teleport(arena.getLocation());
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0);
            }
        });
    }

    private void respawnThen(final Player player, final Runnable action) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                if (player.isDead()) {
                    player.spigot().respawn();
                }
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            return;
                        }
                        action.run();
                    }
                });
            }
        });
    }

    private Location getSpectatorLocation() {
        for (Player player : arena.getAlive()) {
            if (player.isOnline() && !player.isDead()) {
                final Location loc = player.getLocation().clone();
                loc.add(0.0, 3.0, 0.0);
                return loc;
            }
        }
        return arena.getLocation();
    }
}
