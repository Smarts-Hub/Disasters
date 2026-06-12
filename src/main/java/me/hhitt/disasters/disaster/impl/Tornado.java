package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Tornado implements Disaster {

    private static final class State {
        final Arena arena;
        Location center;
        double dirX;
        double dirZ;
        int phase;

        State(Arena arena, Location center) {
            this.arena = arena;
            this.center = center;
            this.dirX = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
            this.dirZ = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
            this.phase = 0;
        }
    }

    private final List<State> states = new ArrayList<>();

    @Override
    public void start(Arena arena) {
        List<Player> alive = arena.getAlive();
        Location targetLoc;
        if (!alive.isEmpty()) {
            targetLoc = alive.get(ThreadLocalRandom.current().nextInt(alive.size())).getLocation();
        } else {
            targetLoc = arena.getLocation();
        }
        Location center = new Location(
            targetLoc.getWorld(),
            targetLoc.getBlockX() + 0.5,
            targetLoc.getBlockY(),
            targetLoc.getBlockZ() + 0.5
        );
        states.add(new State(arena, center));
        Notify.disaster(arena, "tornado");
    }

    @Override
    public void pulse(int time) {
        for (State state : new ArrayList<>(states)) {
            if (time % 1 == 0) {
                state.center = state.center.clone().add(state.dirX * 1.5, 0.0, state.dirZ * 1.5);
                if (!state.arena.getBorderService().isLocationInArena(state.center)) {
                    state.dirX = -state.dirX;
                    state.dirZ = -state.dirZ;
                    state.center = state.center.clone().add(state.dirX * 1.5, 0.0, state.dirZ * 1.5);
                }
            }

            state.phase++;
            spawnSpiralParticles(state);

            for (Player player : new ArrayList<>(state.arena.getAlive())) {
                double dx = player.getLocation().getX() - state.center.getX();
                double dz = player.getLocation().getZ() - state.center.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= 5.0) {
                    double tangentX = -dz / Math.max(dist, 0.1);
                    double tangentZ = dx / Math.max(dist, 0.1);
                    Vector vel = new Vector(tangentX * 1.4, 1.1, tangentZ * 1.4);
                    player.setVelocity(player.getVelocity().add(vel));
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        states.removeAll(statesToRemove(arena));
    }

    private List<State> statesToRemove(Arena arena) {
        List<State> toRemove = new ArrayList<>();
        for (State s : states) {
            if (s.arena == arena) {
                toRemove.add(s);
            }
        }
        return toRemove;
    }

    private void spawnSpiralParticles(State state) {
        World world = state.center.getWorld();
        if (world == null) return;
        for (int i = 0; i < 12; i++) {
            double angle = (state.phase + i * 30) * Math.PI / 30;
            double radius = 1.0 + i * 0.25;
            double x = state.center.getX() + Math.cos(angle) * radius;
            double z = state.center.getZ() + Math.sin(angle) * radius;
            double y = state.center.getY() + i * 0.5;
            world.spawnParticle(Particle.CLOUD, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
