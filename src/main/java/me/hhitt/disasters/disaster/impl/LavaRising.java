package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LavaRising implements Disaster {

    private final List<Arena> arenas = new ArrayList<>();
    private final Map<Arena, Integer> currentY = new HashMap<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        int minY = Math.min(arena.getCorner1().getBlockY(), arena.getCorner2().getBlockY());
        currentY.put(arena, minY);
        Notify.disaster(arena, "lava-rising");
    }

    @Override
    public void pulse(int time) {
        ConfigurationSection config = FileManager.get("config");
        int riseInterval = config != null ? config.getInt("disasters.per-disaster.lava-rising.rise-interval-seconds", 5) : 5;
        int maxBlocks = config != null ? config.getInt("disasters.per-disaster.lava-rising.max-blocks-per-pulse", 2500) : 2500;

        if (time % riseInterval != 0) return;
        for (Arena arena : new ArrayList<>(arenas)) {
            Integer y = currentY.get(arena);
            if (y == null) continue;
            fillLayer(arena, y, maxBlocks);
            currentY.put(arena, y + 1);

            for (Player player : new ArrayList<>(arena.getAlive())) {
                Material feet = player.getLocation().getBlock().getType();
                Material head = player.getLocation().clone().add(0.0, 1.0, 0.0).getBlock().getType();
                if (feet == Material.LAVA || head == Material.LAVA) {
                    DeathMessageService.mark(player, "lava-rising");
                    player.setFireTicks(Math.max(player.getFireTicks(), 80));
                    player.damage(3.0);
                }
            }
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
        int step = (int) Math.max(((long) xRange * zRange) / maxBlocks, 1L);
        int ix = 0;
        while (ix <= xRange && placed < maxBlocks) {
            int iz = 0;
            while (iz <= zRange && placed < maxBlocks) {
                org.bukkit.block.Block block = world.getBlockAt(minX + ix, y, minZ + iz);
                Material type = block.getType();
                if ((type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR)
                    && y + 1 < world.getMaxHeight()
                ) {
                    block.setType(Material.LAVA);
                    placed++;
                }
                iz += step;
            }
            ix += step;
        }
    }
}
