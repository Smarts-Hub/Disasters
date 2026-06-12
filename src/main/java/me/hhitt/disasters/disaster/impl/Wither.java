package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Wither implements Disaster {

    private final Map<Arena, org.bukkit.entity.Wither> spawnWithers = new ConcurrentHashMap<>();
    private final Map<Arena, org.bukkit.entity.Wither> hunterWithers = new ConcurrentHashMap<>();
    private final Map<org.bukkit.entity.Wither, Location> lastLocations = new ConcurrentHashMap<>();
    private final Map<Arena, Integer> startTimes = new ConcurrentHashMap<>();
    private final Map<Arena, Integer> nextRetargetTimes = new ConcurrentHashMap<>();

    @Override
    public void start(Arena arena) {
        org.bukkit.entity.Wither spawnWither = spawnWither(arena.getLocation());
        spawnWithers.put(arena, spawnWither);

        java.util.List<Player> alive = arena.getAlive();
        if (alive.isEmpty()) return;
        Player target = alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
        Location hunterSpawn = getHunterSpawnLocation(target.getLocation());
        org.bukkit.entity.Wither hunterWither = spawnWither(hunterSpawn);
        hunterWither.setTarget(target);
        hunterWithers.put(arena, hunterWither);

        Notify.disaster(arena, "wither");

        startTimes.put(arena, 0);
        nextRetargetTimes.put(arena, ThreadLocalRandom.current().nextInt(1, 31));
    }

    @Override
    public void pulse(int time) {
        for (Map.Entry<Arena, org.bukkit.entity.Wither> entry : hunterWithers.entrySet()) {
            Arena arena = entry.getKey();
            org.bukkit.entity.Wither wither = entry.getValue();
            if (!wither.isDead()) {
                if (time % 3 == 0) {
                    breakBlocksAround(wither, 1);
                }

                int elapsed = (startTimes.containsKey(arena) ? startTimes.get(arena) : 0) + 1;
                startTimes.put(arena, elapsed);

                if (elapsed <= 120) {
                    Integer nextRetarget = nextRetargetTimes.get(arena);
                    if (nextRetarget == null) nextRetarget = 0;
                    if (elapsed >= nextRetarget) {
                        Player nearest = null;
                        double minDist = Double.MAX_VALUE;
                        for (Player p : arena.getAlive()) {
                            double dist = p.getLocation().distanceSquared(wither.getLocation());
                            if (dist < minDist) {
                                minDist = dist;
                                nearest = p;
                            }
                        }
                        if (nearest != null) {
                            wither.setTarget(nearest);
                        }
                        nextRetargetTimes.put(arena, elapsed + ThreadLocalRandom.current().nextInt(1, 31));
                    }
                }
            }
        }

        for (Map.Entry<Arena, org.bukkit.entity.Wither> entry : spawnWithers.entrySet()) {
            org.bukkit.entity.Wither wither = entry.getValue();
            if (!wither.isDead()) {
                Location lastLoc = lastLocations.get(wither);
                Location currentLoc = wither.getLocation();

                if (lastLoc != null && lastLoc.distanceSquared(currentLoc) < 1.0) {
                    breakBlocksAround(wither, 2);
                }

                lastLocations.put(wither, currentLoc);
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        org.bukkit.entity.Wither removed = spawnWithers.remove(arena);
        if (removed != null) {
            lastLocations.remove(removed);
            removed.remove();
        }
        removed = hunterWithers.remove(arena);
        if (removed != null) {
            lastLocations.remove(removed);
            removed.remove();
        }
        startTimes.remove(arena);
        nextRetargetTimes.remove(arena);
    }

    private org.bukkit.entity.Wither spawnWither(Location location) {
        return (org.bukkit.entity.Wither) location.getWorld().spawnEntity(location, EntityType.WITHER);
    }

    private void breakBlocksAround(org.bukkit.entity.Wither wither, int radius) {
        Location loc = wither.getLocation();
        World world = loc.getWorld();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(
                        loc.getBlockX() + x,
                        loc.getBlockY() + y,
                        loc.getBlockZ() + z
                    );
                    if (block.getType() != Material.AIR && !block.isLiquid()) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    private Location getHunterSpawnLocation(Location playerLocation) {
        double angle = ThreadLocalRandom.current().nextDouble(0.0, 2 * Math.PI);
        double horizontalDistance = 30.0;
        double verticalOffset = ThreadLocalRandom.current().nextInt(20, 31);

        double x = playerLocation.getX() + Math.cos(angle) * horizontalDistance;
        double z = playerLocation.getZ() + Math.sin(angle) * horizontalDistance;
        double y = playerLocation.getY() + verticalOffset;

        return new Location(playerLocation.getWorld(), x, y, z);
    }
}
