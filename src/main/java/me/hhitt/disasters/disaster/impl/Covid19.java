package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Covid19 implements Disaster, TriggerTrackedDisaster {

    private int triggerCount = 0;

    private final List<Arena> arenas = new ArrayList<>();
    private final ConcurrentHashMap<Arena, Set<UUID>> infected = new ConcurrentHashMap<>();

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        List<Player> alive = arena.getAlive();
        if (alive.isEmpty()) return;
        Player first = alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
        Set<UUID> set = new HashSet<>();
        set.add(first.getUniqueId());
        infected.put(arena, set);
        Notify.disaster(arena, "covid-19");
        triggerCount = 1;
    }

    @Override
    public void pulse(int time) {
        if (time % 2 != 0) return;
        for (Arena arena : new ArrayList<>(arenas)) {
            Set<UUID> set = infected.get(arena);
            if (set == null) continue;
            Set<UUID> newInfections = new HashSet<>();
            for (Player player : arena.getAlive()) {
                if (set.contains(player.getUniqueId())) {
                    player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0.0, 1.0, 0.0), 5, 0.4, 0.4, 0.4);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, true, false));
                    DeathMessageService.mark(player, "covid-19");
                } else {
                    boolean nearInfected = false;
                    for (Player other : arena.getAlive()) {
                        if (set.contains(other.getUniqueId()) && other.getLocation().distanceSquared(player.getLocation()) <= 9.0) {
                            nearInfected = true;
                            break;
                        }
                    }
                    if (nearInfected && ThreadLocalRandom.current().nextInt(100) < 60) {
                        newInfections.add(player.getUniqueId());
                    }
                }
            }
            set.addAll(newInfections);
        }
    }

    @Override
    public void stop(Arena arena) {
        Set<UUID> set = infected.remove(arena);
        if (set == null) return;
        arenas.remove(arena);
        for (Player player : arena.getAlive()) {
            if (set.contains(player.getUniqueId())) {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.removePotionEffect(PotionEffectType.WEAKNESS);
            }
        }
    }
}
