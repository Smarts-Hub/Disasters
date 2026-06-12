package me.hhitt.disasters.arena.service;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BorderService {

    private final World arenaWorld;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;
    private final int maxY;

    public BorderService(final Location corner1, final Location corner2) {
        this.arenaWorld = corner1.getWorld();
        final double x1 = corner1.getX();
        final double y1 = corner1.getY();
        final double z1 = corner1.getZ();
        final double x2 = corner2.getX();
        final double y2 = corner2.getY();
        final double z2 = corner2.getZ();

        this.minX = (int) Math.min(x1, x2);
        this.maxX = (int) Math.max(x1, x2);
        this.minZ = (int) Math.min(z1, z2);
        this.maxZ = (int) Math.max(z1, z2);
        this.maxY = (int) Math.max(y1, y2);
    }

    public boolean isLocationInArena(final Location loc) {
        final World world = loc.getWorld();
        if (world == null || arenaWorld == null) {
            return false;
        }
        if (!world.getName().equalsIgnoreCase(arenaWorld.getName())) {
            return false;
        }
        return loc.getX() >= minX && loc.getX() <= maxX
            && loc.getY() <= maxY
            && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public boolean isLocationInArenaTp(final Player player) {
        final Location currentLoc = player.getLocation();
        final World world = currentLoc.getWorld();
        if (world == null || arenaWorld == null) {
            return false;
        }
        if (!world.getName().equalsIgnoreCase(arenaWorld.getName())) {
            return false;
        }

        double newX = currentLoc.getX();
        double newY = currentLoc.getY();
        double newZ = currentLoc.getZ();

        if (newX < minX) {
            newX = minX + 0.5;
        } else if (newX > maxX) {
            newX = maxX - 0.5;
        }

        if (newZ < minZ) {
            newZ = minZ + 0.5;
        } else if (newZ > maxZ) {
            newZ = maxZ - 0.5;
        }

        if (newY > maxY) {
            newY = maxY;
        }

        if (newX != currentLoc.getX() || newY != currentLoc.getY() || newZ != currentLoc.getZ()) {
            final Location newLocation = new Location(currentLoc.getWorld(), newX, newY, newZ, currentLoc.getYaw(), currentLoc.getPitch());
            player.teleport(newLocation);
        }

        return newX >= minX && newX <= maxX && newY <= maxY && newZ >= minZ && newZ <= maxZ;
    }
}
