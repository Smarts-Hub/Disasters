package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldBorder implements Disaster {

    private final Map<Arena, Double> arenaSizes = new ConcurrentHashMap<>();
    private final double shrinkAmountPerPulse = 0.5;

    @Override
    public void start(Arena arena) {
        Notify.disaster(arena, "world-border");
        Location corner1 = arena.getCorner1();
        Location corner2 = arena.getCorner2();
        Location center = new Location(
            corner1.getWorld(),
            (corner1.getX() + corner2.getX()) / 2,
            (corner1.getY() + corner2.getY()) / 2,
            (corner1.getZ() + corner2.getZ()) / 2
        );
        double initialRadius = corner1.distance(corner2) / 2;
        arenaSizes.put(arena, initialRadius);

        for (Player player : arena.getPlaying()) {
            sendWorldBorder(player, center, initialRadius);
        }
    }

    @Override
    public void pulse(int time) {
        if (time % 5 != 0) return;
        for (Map.Entry<Arena, Double> entry : arenaSizes.entrySet()) {
            Arena arena = entry.getKey();
            double currentRadius = entry.getValue();

            Location corner1 = arena.getCorner1();
            Location corner2 = arena.getCorner2();
            Location center = new Location(
                corner1.getWorld(),
                (corner1.getX() + corner2.getX()) / 2,
                (corner1.getY() + corner2.getY()) / 2,
                (corner1.getZ() + corner2.getZ()) / 2
            );

            currentRadius -= shrinkAmountPerPulse;
            if (currentRadius < 5.0) {
                currentRadius = 5.0;
            }
            arenaSizes.put(arena, currentRadius);

            for (Player player : arena.getPlaying()) {
                updateWorldBorder(player, center, currentRadius);
            }

            checkPlayersOutsideBorderAndApplyDamage(arena, center, currentRadius);
        }
    }

    @Override
    public void stop(Arena arena) {
        arenaSizes.remove(arena);
        for (Player player : arena.getPlaying()) {
            resetWorldBorder(player);
        }
    }

    private void sendWorldBorder(Player player, Location center, double size) {
        org.bukkit.WorldBorder worldBorder = Bukkit.createWorldBorder();
        worldBorder.setCenter(center.getX(), center.getZ());
        worldBorder.setSize(size * 2);
        worldBorder.setDamageAmount(0.0);
        worldBorder.setDamageBuffer(0.0);
        player.setWorldBorder(worldBorder);
    }

    private void updateWorldBorder(Player player, Location center, double size) {
        sendWorldBorder(player, center, size);
    }

    private void resetWorldBorder(Player player) {
        player.setWorldBorder(null);
    }

    private void checkPlayersOutsideBorderAndApplyDamage(Arena arena, Location center, double radius) {
        double radiusSquared = radius * radius;
        for (Player player : arena.getAlive()) {
            Location loc = player.getLocation();
            if (loc.getWorld().equals(center.getWorld())) {
                double distanceSquared = loc.distanceSquared(center);
                if (distanceSquared > radiusSquared) {
                    player.damage(2.0);
                }
            }
        }
    }
}
