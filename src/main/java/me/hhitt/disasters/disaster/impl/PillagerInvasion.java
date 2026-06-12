package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import me.hhitt.disasters.util.SpawnLocationFinder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PillagerInvasion implements Disaster, TriggerTrackedDisaster {

    private int triggerCount;

    private final List<Arena> arenas = new ArrayList<>();
    private final Map<Arena, List<Pillager>> pillagers = new HashMap<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        pillagers.put(arena, new ArrayList<Pillager>());
        Notify.disaster(arena, "pillager-invasion");
    }

    @Override
    public void pulse(int time) {
        if (time % 10 != 0) return;
        for (Arena arena : new ArrayList<>(arenas)) {
            List<Pillager> list = pillagers.get(arena);
            if (list == null) continue;
            list.removeAll(deadPillagers(list));
            if (list.size() > 30) continue;
            for (Player player : new ArrayList<>(arena.getAlive())) {
                Location spawnLoc = SpawnLocationFinder.findNearPlayer(arena, player.getLocation(), 8, 14, 3);
                if (spawnLoc == null) continue;
                Player target = player;
                double minDist = Double.MAX_VALUE;
                for (Player p : arena.getAlive()) {
                    if (p == player) continue;
                    double dist = p.getLocation().distanceSquared(spawnLoc);
                    if (dist < minDist) {
                        minDist = dist;
                        target = p;
                    }
                }
                Pillager pillager = spawnLoc.getWorld().spawn(spawnLoc, Pillager.class);
                pillager.getInventory().setItem(0, new ItemStack(Material.CROSSBOW));
                pillager.setTarget(target);
                list.add(pillager);
                triggerCount++;
            }
        }
    }

    private List<Pillager> deadPillagers(List<Pillager> list) {
        List<Pillager> dead = new ArrayList<>();
        for (Pillager p : list) {
            if (p.isDead()) dead.add(p);
        }
        return dead;
    }

    @Override
    public void stop(Arena arena) {
        List<Pillager> list = pillagers.remove(arena);
        if (list != null) {
            for (Pillager p : list) {
                if (!p.isDead()) p.remove();
            }
        }
        arenas.remove(arena);
    }

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }
}
