package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.CopyOnWriteArrayList;

public class HotSun implements Disaster {

    private final CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();

    @Override
    public void start(final Arena arena) {
        for (Player player : arena.getPlaying()) {
            players.add(player);
        }
        Notify.disaster(arena, "hot-sun");
    }

    @Override
    public void pulse(final int time) {
        if (time % 2 != 0) {
            return;
        }

        final Configuration cfg = FileManager.get("config");
        final double damageAmount = cfg != null ? cfg.getDouble("hot-sun-damage", 0.5) : 0.5;

        for (Player player : players) {
            final Location loc = player.getLocation();

            final boolean exposed = loc.getBlock().getLightFromSky() >= 15
                && player.getWorld().getHighestBlockAt(loc).getY() <= loc.getBlockY();

            if (exposed) {
                player.damage(damageAmount);
            }
        }
    }

    @Override
    public void stop(final Arena arena) {
        for (Player player : arena.getPlaying()) {
            players.remove(player);
        }
    }
}
