package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Lightning implements Disaster, TriggerTrackedDisaster {

    private int triggerCount;

    private final List<Arena> arenas = new CopyOnWriteArrayList<>();
    private final int radius = 5;
    private final List<BukkitTask> tasks = new ArrayList<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        Notify.disaster(arena, "lightning");
    }

    @Override
    public void pulse(int time) {
        if (time % 3 != 0) return;
        for (Arena arena : arenas) {
            List<Player> alive = arena.getAlive();
            if (alive.isEmpty()) continue;
            Player target = alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
            Location playerLocation = target.getLocation();
            double offsetX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * radius;
            double offsetZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * radius;
            Location strikeLocation = playerLocation.clone().add(offsetX, 0.0, offsetZ);
            World world = strikeLocation.getWorld();
            if (world == null) continue;
            double highestBlockY = world.getHighestBlockYAt(strikeLocation);
            strikeLocation.setY(highestBlockY);

            for (int i = 0; i < 12; i++) {
                double angle = i * (Math.PI * 2 / 12);
                double px = strikeLocation.getX() + Math.cos(angle) * 1.5;
                double pz = strikeLocation.getZ() + Math.sin(angle) * 1.5;
                world.spawnParticle(
                    Particle.DUST,
                    px, strikeLocation.getY() + 0.5, pz,
                    2, 0.1, 0.0, 0.1,
                    new Particle.DustOptions(Color.YELLOW, 1.5f)
                );
            }
            world.playSound(strikeLocation, Sound.ENTITY_BEE_LOOP, 1f, 1.5f);

            BukkitTask task = Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), new Runnable() {
                @Override
                public void run() {
                    for (Player player : arena.getAlive()) {
                        if (player.getLocation().distanceSquared(strikeLocation) <= 16.0) {
                            DeathMessageService.mark(player, "lightning");
                        }
                    }
                    world.strikeLightning(strikeLocation);
                    world.createExplosion(strikeLocation, 2f, false, true);
                    triggerCount++;
                }
            }, 20L);
            tasks.add(task);
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
}
