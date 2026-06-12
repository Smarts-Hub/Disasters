package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Landmine implements Disaster, TriggerTrackedDisaster, Listener {

    private int triggerCount;

    private static final class MineKey {
        final String world;
        final int x;
        final int y;
        final int z;

        MineKey(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MineKey)) return false;
            MineKey mineKey = (MineKey) o;
            return x == mineKey.x && y == mineKey.y && z == mineKey.z && world.equals(mineKey.world);
        }

        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }

    private final Map<Arena, Set<MineKey>> mines = new ConcurrentHashMap<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final List<Arena> arenas = new ArrayList<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        mines.put(arena, ConcurrentHashMap.newKeySet());
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        Notify.disaster(arena, "landmine");
    }

    @Override
    public void pulse(int time) {
        if (time % 5 != 0) return;
        for (Arena arena : new ArrayList<>(arenas)) {
            Set<MineKey> set = mines.get(arena);
            if (set == null) continue;
            for (org.bukkit.entity.Player player : new ArrayList<>(arena.getAlive())) {
                Location origin = player.getLocation();
                for (int i = 0; i < 2; i++) {
                    int dx = ThreadLocalRandom.current().nextInt(-5, 6);
                    int dz = ThreadLocalRandom.current().nextInt(-5, 6);
                    int x = origin.getBlockX() + dx;
                    int z = origin.getBlockZ() + dz;
                    org.bukkit.World world = origin.getWorld();
                    int y = world.getHighestBlockYAt(x, z);
                    MineKey key = new MineKey(world.getName(), x, y, z);
                    if (arena.getBorderService().isLocationInArena(new Location(world, x, y, z))) {
                        set.add(key);
                    }
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
        mines.remove(arena);
        arenas.remove(arena);
        HandlerList.unregisterAll(this);
    }

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Arena found = null;
        for (Arena a : arenas) {
            if (a.isPlayerValid(event.getPlayer())) {
                found = a;
                break;
            }
        }
        if (found == null) return;
        final Arena arena = found;
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;
        Set<MineKey> set = mines.get(arena);
        if (set == null) return;
        MineKey key = new MineKey(event.getTo().getWorld().getName(), event.getTo().getBlockX(), event.getTo().getBlockY() - 1, event.getTo().getBlockZ());
        if (set.remove(key)) {
            Location center = event.getTo().clone().subtract(0.0, 1.0, 0.0).add(0.5, 0.5, 0.5);
            event.getPlayer().getWorld().playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            BukkitTask task = Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().getWorld().createExplosion(center, 4f, false, true);
                    for (org.bukkit.entity.Player p : new ArrayList<>(arena.getAlive())) {
                        if (p.getLocation().distance(center) <= 5.0) {
                            DeathMessageService.mark(p, "landmine");
                        }
                    }
                    triggerCount++;
                }
            }, 20L);
            tasks.add(task);
        }
    }
}
