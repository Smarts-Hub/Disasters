package me.hhitt.disasters.util;

import me.hhitt.disasters.arena.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public final class SpawnLocationFinder {

    private static final int[][] DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private SpawnLocationFinder() {
    }

    public static Location findNearPlayer(Arena arena, Location origin, int minDistance, int maxRadius, int verticalSearch) {
        Location result = findSameY(arena, origin, minDistance, maxRadius);
        if (result != null) {
            return result;
        }
        result = findAroundXZWithVerticalSearch(arena, origin, minDistance, maxRadius, verticalSearch);
        if (result != null) {
            return result;
        }
        result = findAdjacent(arena, origin);
        if (result != null) {
            return result;
        }
        return origin.clone().add(1.0, 0.0, 0.0);
    }

    private static Location findSameY(Arena arena, Location origin, int minDistance, int maxRadius) {
        World world = origin.getWorld();
        if (world == null) {
            return null;
        }
        for (int distance = minDistance; distance <= maxRadius; distance++) {
            for (int[] dir : DIRECTIONS) {
                int dx = dir[0];
                int dz = dir[1];
                Location loc = new Location(
                    world,
                    origin.getBlockX() + dx * distance + 0.5,
                    (double) origin.getBlockY(),
                    origin.getBlockZ() + dz * distance + 0.5
                );
                if (isSafe(arena, loc)) {
                    return loc;
                }
            }
        }
        return null;
    }

    private static Location findAroundXZWithVerticalSearch(Arena arena, Location origin, int minDistance, int maxRadius, int verticalSearch) {
        World world = origin.getWorld();
        if (world == null) {
            return null;
        }
        for (int distance = minDistance; distance <= maxRadius; distance++) {
            for (int[] dir : DIRECTIONS) {
                int dx = dir[0];
                int dz = dir[1];
                int x = origin.getBlockX() + dx * distance;
                int z = origin.getBlockZ() + dz * distance;
                Integer y = findGroundNearY(world, x, z, origin.getBlockY(), verticalSearch);
                if (y != null) {
                    Location loc = new Location(world, x + 0.5, y + 1.0, z + 0.5);
                    if (isSafe(arena, loc)) {
                        return loc;
                    }
                }
            }
        }
        return null;
    }

    private static Location findAdjacent(Arena arena, Location origin) {
        World world = origin.getWorld();
        if (world == null) {
            return null;
        }
        for (int[] dir : DIRECTIONS) {
            int dx = dir[0];
            int dz = dir[1];
            Location loc = new Location(
                world,
                origin.getBlockX() + dx + 0.5,
                (double) origin.getBlockY(),
                origin.getBlockZ() + dz + 0.5
            );
            if (isSafe(arena, loc)) {
                return loc;
            }
        }
        return null;
    }

    private static Integer findGroundNearY(World world, int x, int z, int startY, int verticalSearch) {
        for (int offset = 0; offset <= verticalSearch; offset++) {
            int down = startY - offset;
            if (hasGround(world, x, down, z)) {
                return down;
            }
            int up = startY + offset;
            if (offset != 0 && hasGround(world, x, up, z)) {
                return up;
            }
        }
        return null;
    }

    private static boolean hasGround(World world, int x, int y, int z) {
        org.bukkit.block.Block ground = world.getBlockAt(x, y - 1, z);
        org.bukkit.block.Block feet = world.getBlockAt(x, y, z);
        org.bukkit.block.Block head = world.getBlockAt(x, y + 1, z);
        return ground.getType().isSolid()
            && ground.getType() != Material.LAVA
            && ground.getType() != Material.CACTUS
            && feet.getType().isAir()
            && head.getType().isAir();
    }

    public static boolean isSafe(Arena arena, Location location) {
        if (!arena.getBorderService().isLocationInArena(location)) {
            return false;
        }
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        return hasGround(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
