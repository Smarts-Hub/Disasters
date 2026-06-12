package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TntRain implements Disaster {

    private final List<Arena> arenas = new ArrayList<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        Notify.disaster(arena, "tnt-rain");
    }

    @Override
    public void pulse(int time) {
        if (time % 2 != 0) return;
        for (Arena arena : new ArrayList<>(arenas)) {
            for (org.bukkit.entity.Player player : new ArrayList<>(arena.getAlive())) {
                if (ThreadLocalRandom.current().nextInt(100) < 50) {
                    org.bukkit.Location target = player.getLocation().clone().add(
                        ThreadLocalRandom.current().nextDouble(-12.0, 12.0), 0.0, ThreadLocalRandom.current().nextDouble(-12.0, 12.0)
                    );
                    if (!arena.getBorderService().isLocationInArena(target)) continue;
                    TNTPrimed tnt = target.getWorld().spawn(
                        target.clone().add(0.0, ThreadLocalRandom.current().nextDouble(18.0, 25.0), 0.0),
                        TNTPrimed.class
                    );
                    tnt.setFuseTicks(60);
                    tnt.setYield(4f);
                    tnt.setVelocity(new Vector(0.0, -0.5, 0.0));
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        arenas.remove(arena);
    }
}
