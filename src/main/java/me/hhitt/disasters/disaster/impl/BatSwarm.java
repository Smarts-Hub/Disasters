package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BatSwarm implements Disaster {

    private final List<Arena> arenas = new ArrayList<>();
    private final Map<Arena, List<Bat>> bats = new HashMap<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        bats.put(arena, new ArrayList<Bat>());
        spawnWave(arena, ThreadLocalRandom.current().nextInt(8, 13));
        Notify.disaster(arena, "bat-swarm");
    }

    @Override
    public void pulse(int time) {
        if (time > 0 && time % 8 == 0) {
            for (Arena arena : new ArrayList<>(arenas)) {
                spawnWave(arena, ThreadLocalRandom.current().nextInt(8, 13));
            }
        }
        for (Arena arena : new ArrayList<>(arenas)) {
            List<Bat> list = bats.get(arena);
            if (list == null) continue;
            list.removeIf(Bat::isDead);
            for (Player player : arena.getAlive()) {
                int nearby = 0;
                for (Bat bat : list) {
                    if (bat.getLocation().distanceSquared(player.getLocation()) <= 16.0) {
                        nearby++;
                    }
                }
                if (nearby > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, true, false));
                    DeathMessageService.mark(player, "bat-swarm");
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        List<Bat> removed = bats.remove(arena);
        if (removed != null) {
            for (Bat bat : removed) {
                if (!bat.isDead()) bat.remove();
            }
        }
        arenas.remove(arena);
    }

    private void spawnWave(Arena arena, int count) {
        List<Bat> list = bats.get(arena);
        if (list == null) return;
        for (Player player : arena.getAlive()) {
            for (int i = 0; i < count; i++) {
                Location spawn = player.getLocation().clone().add(
                    ThreadLocalRandom.current().nextDouble(-4.0, 4.0),
                    ThreadLocalRandom.current().nextDouble(2.0, 6.0),
                    ThreadLocalRandom.current().nextDouble(-4.0, 4.0)
                );
                if (!arena.getBorderService().isLocationInArena(spawn)) continue;
                Bat bat = spawn.getWorld().spawn(spawn, Bat.class);
                list.add(bat);
            }
        }
    }
}
