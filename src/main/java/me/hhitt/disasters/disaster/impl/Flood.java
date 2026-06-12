package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Flood implements Disaster {

    private final List<Arena> arenas = new ArrayList<>();
    private final Map<Arena, Integer> currentY = new HashMap<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        int minY = Math.min(arena.getCorner1().getBlockY(), arena.getCorner2().getBlockY());
        currentY.put(arena, minY);
        Notify.disaster(arena, "flood");
    }

    @Override
    public void pulse(int time) {
        Configuration config = FileManager.get("config");
        int riseInterval = config != null ? config.getInt("disasters.per-disaster.flood.rise-interval-seconds", 4) : 4;
        int maxBlocks = config != null ? config.getInt("disasters.per-disaster.flood.max-blocks-per-pulse", 2500) : 2500;

        if (time % riseInterval != 0) return;
        for (Arena arena : new ArrayList<>(arenas)) {
            Integer y = currentY.get(arena);
            if (y == null) continue;
            fillLayer(arena, y, maxBlocks);
            currentY.put(arena, y + 1);
        }
    }

    @Override
    public void stop(Arena arena) {
        arenas.remove(arena);
        currentY.remove(arena);
    }

    private void fillLayer(Arena arena, int y, int maxBlocks) {
        World world = arena.getCorner1().getWorld();
        if (world == null) return;
        int minX = Math.min(arena.getCorner1().getBlockX(), arena.getCorner2().getBlockX());
        int maxX = Math.max(arena.getCorner1().getBlockX(), arena.getCorner2().getBlockX());
        int minZ = Math.min(arena.getCorner1().getBlockZ(), arena.getCorner2().getBlockZ());
        int maxZ = Math.max(arena.getCorner1().getBlockZ(), arena.getCorner2().getBlockZ());
        int placed = 0;
        int xRange = Math.max(maxX - minX, 1);
        int zRange = Math.max(maxZ - minZ, 1);
        int step = (int) Math.max((((long) xRange * zRange) / maxBlocks), 1L);
        for (int ix = 0; ix <= xRange && placed < maxBlocks; ix += step) {
            for (int iz = 0; iz <= zRange && placed < maxBlocks; iz += step) {
                Block block = world.getBlockAt(minX + ix, y, minZ + iz);
                if ((block.getType().isAir() || block.getType() == Material.CAVE_AIR || block.getType() == Material.VOID_AIR)
                    && y + 1 < world.getMaxHeight()) {
                    block.setType(Material.WATER);
                    placed++;
                }
            }
        }
    }
}
