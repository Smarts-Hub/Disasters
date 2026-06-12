package me.hhitt.disasters.model.entity;

import me.hhitt.disasters.arena.Arena;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TNTPrimed;

public class DisasterSheep {

    private final Arena arena;
    private final Sheep sheep;
    private int tick = 3;

    public DisasterSheep(final Arena arena, final Sheep sheep) {
        this.arena = arena;
        this.sheep = sheep;
        sheep.setInvulnerable(true);
        sheep.setColor(DyeColor.GREEN);
        sheep.setAI(true);
    }

    public Sheep getSheep() {
        return sheep;
    }

    public void call() {
        switch (tick) {
            case 3:
                sheep.setColor(DyeColor.GREEN);
                break;
            case 2:
                sheep.setColor(DyeColor.ORANGE);
                break;
            case 1:
                sheep.setColor(DyeColor.RED);
                break;
            case 0:
                Location loc = sheep.getLocation();
                spawnTNTExplosion(loc);
                remove();
                return;
        }
        tick--;
        seekNearestPlayer();
    }

    private void seekNearestPlayer() {
        if (!sheep.isValid() || sheep.isDead()) {
            return;
        }
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player player : arena.getAlive()) {
            if (!player.isOnline() || player.isDead()) {
                continue;
            }
            Location playerLoc = player.getLocation();
            if (!playerLoc.getWorld().equals(sheep.getWorld())) {
                continue;
            }
            double dist = sheep.getLocation().distanceSquared(playerLoc);
            if (dist < nearestDistance) {
                nearestDistance = dist;
                nearest = player;
            }
        }
        if (nearest != null) {
            sheep.getPathfinder().moveTo(nearest, 1.25D);
        } else {
            sheep.getPathfinder().stopPathfinding();
        }
    }

    public boolean isAlive() {
        return sheep.isValid() && !sheep.isDead();
    }

    public void remove() {
        sheep.getPathfinder().stopPathfinding();
        for (Entity passenger : sheep.getPassengers()) {
            passenger.remove();
        }
        sheep.remove();
    }

    private void spawnTNTExplosion(final Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        TNTPrimed tnt = world.spawn(location, TNTPrimed.class);
        tnt.setFuseTicks(0);
        tnt.setYield(4.0f);
    }
}
