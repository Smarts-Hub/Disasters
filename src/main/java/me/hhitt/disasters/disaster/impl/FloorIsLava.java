package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.model.block.DisasterFloor;
import me.hhitt.disasters.util.Notify;

import java.util.concurrent.ConcurrentHashMap;

public class FloorIsLava implements Disaster {

    private final ConcurrentHashMap<String, DisasterFloor> blocks = new ConcurrentHashMap<>();
    private final int duration = 120;
    private int elapsed = 0;
    private boolean active = true;

    @Override
    public void start(Arena arena) {
        Notify.disaster(arena, "floor-is-lava");
    }

    @Override
    public void pulse(int time) {
        elapsed++;
        if (elapsed >= duration) {
            active = false;
        }
        for (DisasterFloor block : blocks.values()) {
            block.updateMaterial();
        }
    }

    @Override
    public void stop(Arena arena) {
        blocks.clear();
    }

    public boolean isActive() {
        return active;
    }

    public void addBlock(DisasterFloor block) {
        if (!active) return;
        String key = locationKey(block);
        if (blocks.containsKey(key)) return;
        blocks.put(key, block);
    }

    public void removeBlock(DisasterFloor block) {
        blocks.remove(locationKey(block));
    }

    private String locationKey(DisasterFloor block) {
        return block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
    }
}
