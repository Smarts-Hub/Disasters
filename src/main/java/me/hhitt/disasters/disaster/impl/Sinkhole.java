package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Sinkhole implements Disaster, TriggerTrackedDisaster, Listener {

    private int triggerCount;

    private static final class Position {
        final int x;
        final int y;
        final int z;

        Position(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private final List<Arena> arenas = new ArrayList<>();
    private final List<BukkitTask> tasks = new ArrayList<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        Notify.disaster(arena, "sinkhole");
    }

    @Override
    public void pulse(int time) {
        if (time % 4 != 0) return;
        for (Arena arena : new ArrayList<>(arenas)) {
            List<org.bukkit.entity.Player> alive = arena.getAlive();
            if (alive.isEmpty()) continue;
            org.bukkit.entity.Player target = alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
            org.bukkit.block.Block center = target.getLocation().getBlock();
            World world = center.getWorld();
            int radius = 2;
            warnAndCollapse(arena, world, center.getX(), center.getY(), center.getZ(), radius);
        }
    }

    @Override
    public void stop(Arena arena) {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
        arenas.remove(arena);
        HandlerList.unregisterAll(this);
    }

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }

    private void warnAndCollapse(Arena arena, World world, int cx, int cy, int cz, int radius) {
        final List<Position> positions = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                positions.add(new Position(cx + dx, cy, cz + dz));
            }
        }

        BukkitTask t1 = Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Position p : positions) {
                    world.getBlockAt(p.x, p.y, p.z).setType(Material.YELLOW_CONCRETE);
                }
            }
        }, 10L);
        BukkitTask t2 = Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Position p : positions) {
                    world.getBlockAt(p.x, p.y, p.z).setType(Material.ORANGE_CONCRETE);
                }
            }
        }, 20L);
        BukkitTask t3 = Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Position p : positions) {
                    world.getBlockAt(p.x, p.y, p.z).setType(Material.RED_CONCRETE);
                }
            }
        }, 30L);
        BukkitTask t4 = Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Position p : positions) {
                    world.getBlockAt(p.x, p.y, p.z).setType(Material.AIR);
                    if (p.y - 5 > 0) {
                        world.getBlockAt(p.x, p.y - 5, p.z).setType(Material.LAVA);
                    }
                }
                for (org.bukkit.entity.Player player : new ArrayList<>(arena.getAlive())) {
                    if (Math.abs(player.getLocation().getBlockX() - cx) <= radius
                        && Math.abs(player.getLocation().getBlockZ() - cz) <= radius
                        && Math.abs(player.getLocation().getBlockY() - cy) <= 2
                    ) {
                        DeathMessageService.mark(player, "sinkhole");
                        player.damage(8.0);
                    }
                }
                triggerCount++;
            }
        }, 30L);
        tasks.add(t1);
        tasks.add(t2);
        tasks.add(t3);
        tasks.add(t4);
    }

}
