package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Nuke implements Disaster, TriggerTrackedDisaster {

    private int triggerCount;

    private final List<Arena> arenas = new ArrayList<>();
    private final List<BukkitTask> tasks = new ArrayList<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        Notify.disaster(arena, "nuke");
    }

    @Override
    public void pulse(int time) {
        for (Arena arena : new ArrayList<>(arenas)) {
            if (time == 5) colorQuadrant(arena, Material.YELLOW_TERRACOTTA);
            if (time == 10) colorQuadrant(arena, Material.ORANGE_TERRACOTTA);
            if (time == 15) {
                colorQuadrant(arena, Material.RED_TERRACOTTA);
                for (Player player : arena.getPlaying()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1f, 0.5f);
                }
            }
            if (time == 20) detonate(arena);
        }
    }

    @Override
    public void stop(Arena arena) {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
        arenas.remove(arena);
    }

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }

    private void colorQuadrant(Arena arena, Material material) {
        World world = arena.getCorner1().getWorld();
        if (world == null) return;
        int centerX = (arena.getCorner1().getBlockX() + arena.getCorner2().getBlockX()) / 2;
        int centerZ = (arena.getCorner1().getBlockZ() + arena.getCorner2().getBlockZ()) / 2;
        int minX = Math.min(arena.getCorner1().getBlockX(), arena.getCorner2().getBlockX());
        int maxX = Math.max(arena.getCorner1().getBlockX(), arena.getCorner2().getBlockX());
        int minZ = Math.min(arena.getCorner1().getBlockZ(), arena.getCorner2().getBlockZ());
        int maxZ = Math.max(arena.getCorner1().getBlockZ(), arena.getCorner2().getBlockZ());
        int startX = centerX - 0 >= 0 ? centerX : minX;
        int endX = centerX - 0 >= 0 ? maxX : centerX;
        int startZ = centerZ - 0 >= 0 ? centerZ : minZ;
        int endZ = centerZ - 0 >= 0 ? maxZ : centerZ;
        int placed = 0;
        int cap = 2500;
        outer:
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                if (placed >= cap) break outer;
                int y = world.getHighestBlockYAt(x, z);
                org.bukkit.block.Block block = world.getBlockAt(x, y, z);
                if (block.getType().isSolid()) {
                    block.setType(material);
                    placed++;
                }
            }
        }
    }

    private void detonate(Arena arena) {
        World world = arena.getCorner1().getWorld();
        if (world == null) return;
        int centerX = (arena.getCorner1().getBlockX() + arena.getCorner2().getBlockX()) / 2;
        int centerZ = (arena.getCorner1().getBlockZ() + arena.getCorner2().getBlockZ()) / 2;
        int minX = Math.min(arena.getCorner1().getBlockX(), arena.getCorner2().getBlockX());
        int maxX = Math.max(arena.getCorner1().getBlockX(), arena.getCorner2().getBlockX());
        int minZ = Math.min(arena.getCorner1().getBlockZ(), arena.getCorner2().getBlockZ());
        int maxZ = Math.max(arena.getCorner1().getBlockZ(), arena.getCorner2().getBlockZ());
        int startX = centerX - 0 >= 0 ? centerX : minX;
        int endX = centerX - 0 >= 0 ? maxX : centerX;
        int startZ = centerZ - 0 >= 0 ? centerZ : minZ;
        int endZ = centerZ - 0 >= 0 ? maxZ : centerZ;
        for (Player player : new ArrayList<>(arena.getAlive())) {
            if (player.getLocation().getBlockX() >= startX && player.getLocation().getBlockX() <= endX
                && player.getLocation().getBlockZ() >= startZ && player.getLocation().getBlockZ() <= endZ) {
                DeathMessageService.mark(player, "nuke");
            }
        }
        int x = startX;
        while (x <= endX) {
            int z = startZ;
            while (z <= endZ) {
                int y = world.getHighestBlockYAt(x, z);
                world.createExplosion(world.getBlockAt(x, y, z).getLocation().add(0.5, 0.5, 0.5), 6f, false, true);
                world.getBlockAt(x, y, z).setType(Material.AIR);
                z += 8;
            }
            x += 8;
        }
        triggerCount = 1;
    }
}
