package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.DisasterRegistry;
import me.hhitt.disasters.model.block.DisappearBlock;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class BlockDisappear implements Disaster {

    private final ConcurrentHashMap<String, DisappearBlock> blocks = new ConcurrentHashMap<>();
    private Arena arena;
    private final int duration = 120;
    private int elapsed = 0;
    private boolean active = true;

    @Override
    public void start(Arena arena) {
        this.arena = arena;
        Notify.disaster(arena, "disappear-blocks");
    }

    @Override
    public void pulse(int time) {
        elapsed++;
        if (elapsed >= duration) {
            active = false;
        }
        for (DisappearBlock block : blocks.values()) {
            block.updateMaterial();
        }

        Arena currentArena = arena;
        if (currentArena == null) return;
        if (!active) return;
        for (Player player : currentArena.getAlive()) {
            Location loc = player.getLocation();
            Location blockBelow = loc.clone().subtract(0.0, 1.0, 0.0);
            if (blockBelow.getBlock().getType().isAir()) {
                double[] offsets = new double[]{-0.3, 0.3};
                outer:
                for (double dx : offsets) {
                    for (double dz : offsets) {
                        Location check = loc.clone().add(dx, -1.0, dz);
                        if (!check.getBlock().getType().isAir() && check.getBlock().getType().isSolid()) {
                            DisasterRegistry.addBlockToDisappear(currentArena, loc.clone().add(dx, 0.0, dz));
                            break outer;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        blocks.clear();
        this.arena = null;
    }

    public boolean isActive() {
        return active;
    }

    public void addBlock(Arena arena, Location location) {
        if (!active) return;
        String key = locationKey(location);
        DisappearBlock existing = blocks.get(key);
        if (existing != null) {
            existing.setOccupied(true);
            return;
        }
        DisappearBlock block = new DisappearBlock(arena, location);
        blocks.put(key, block);
    }

    public void setUnoccupied(Location location) {
        String key = locationKey(location);
        DisappearBlock block = blocks.get(key);
        if (block != null) {
            block.setOccupied(false);
        }
    }

    public void removeBlock(DisappearBlock block) {
        blocks.remove(locationKey(block.getLocation()));
    }

    private String locationKey(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}
