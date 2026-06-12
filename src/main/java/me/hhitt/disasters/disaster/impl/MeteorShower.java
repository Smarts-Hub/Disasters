package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MeteorShower implements Disaster {

    private static final class MeteorTracker {
        final FallingBlock entity;
        final Location target;
        int ticks;
        double lastY;

        MeteorTracker(FallingBlock entity, Location target) {
            this.entity = entity;
            this.target = target;
            this.ticks = 0;
            this.lastY = Double.MAX_VALUE;
        }
    }

    private final List<Arena> arenas = new ArrayList<>();
    private final Map<Arena, List<MeteorTracker>> activeMeteors = new HashMap<>();
    private final int duration = 180;
    private int elapsed = 0;
    private boolean active = true;

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        activeMeteors.put(arena, new ArrayList<MeteorTracker>());
        Notify.disaster(arena, "meteor-shower");
    }

    @Override
    public void pulse(int time) {
        elapsed++;
        if (elapsed >= duration) {
            active = false;
        }

        for (Arena arena : new ArrayList<>(arenas)) {
            List<MeteorTracker> meteors = activeMeteors.get(arena);
            if (meteors == null) continue;

            if (active) {
                for (Player player : new ArrayList<>(arena.getAlive())) {
                    if (ThreadLocalRandom.current().nextInt(3) != 0) continue;

                    World world = player.getWorld();

                    Location target = player.getLocation().clone().add(
                        ThreadLocalRandom.current().nextDouble(-8.0, 8.0) + player.getVelocity().getX() * 5,
                        0.0,
                        ThreadLocalRandom.current().nextDouble(-8.0, 8.0) + player.getVelocity().getZ() * 5
                    );
                    target.setY(world.getHighestBlockYAt(target.getBlockX(), target.getBlockZ()) + 1);

                    Location spawn = target.clone().add(
                        ThreadLocalRandom.current().nextDouble(-3.0, 3.0),
                        ThreadLocalRandom.current().nextInt(20, 30),
                        ThreadLocalRandom.current().nextDouble(-3.0, 3.0)
                    );

                    FallingBlock meteor = world.spawn(spawn, FallingBlock.class);
                    meteor.setBlockData(Material.MAGMA_BLOCK.createBlockData());
                    meteor.setDropItem(false);
                    meteor.setHurtEntities(false);
                    meteor.setSilent(true);

                    Vector direction = target.toVector()
                        .subtract(spawn.toVector())
                        .normalize()
                        .multiply(1.8);
                    meteor.setVelocity(direction);

                    for (int i = 0; i <= 12; i++) {
                        double angle = i * (Math.PI * 2 / 12);
                        double px = target.getX() + Math.cos(angle) * 2.0;
                        double pz = target.getZ() + Math.sin(angle) * 2.0;
                        world.spawnParticle(
                            Particle.DUST,
                            px, target.getY() + 0.2, pz,
                            2, 0.1, 0.0, 0.1,
                            new Particle.DustOptions(Color.RED, 1.5f)
                        );
                    }

                    world.playSound(spawn, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.4f);

                    meteors.add(new MeteorTracker(meteor, target));
                }
            }

            Iterator<MeteorTracker> iter = meteors.iterator();
            while (iter.hasNext()) {
                MeteorTracker m = iter.next();
                m.ticks++;

                if (m.entity.isDead()) {
                    createImpact(arena, m.entity.getLocation());
                    iter.remove();
                    continue;
                }

                Location loc = m.entity.getLocation();
                World world = loc.getWorld();

                world.spawnParticle(Particle.FLAME, loc, 6, 0.2, 0.2, 0.2, 0.02);
                world.spawnParticle(Particle.SMOKE, loc, 4, 0.15, 0.15, 0.15, 0.01);
                world.spawnParticle(Particle.LAVA, loc, 1, 0.1, 0.1, 0.1);

                double groundY = world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
                boolean stopped = Math.abs(loc.getY() - m.lastY) < 0.05 && m.ticks > 5;
                boolean belowGround = loc.getY() <= groundY + 1.0 && m.ticks > 5;
                boolean timedOut = m.ticks > 80;

                m.lastY = loc.getY();

                if (stopped || belowGround || timedOut) {
                    createImpact(arena, loc);
                    m.entity.remove();
                    iter.remove();
                }
            }
        }
    }

    private void createImpact(Arena arena, Location location) {
        World world = location.getWorld();

        world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 2);
        world.spawnParticle(Particle.FLAME, location, 40, 2.5, 1.5, 2.5, 0.1);
        world.spawnParticle(Particle.LAVA, location, 20, 2.0, 0.5, 2.0);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 15, 1.5, 1.5, 1.5, 0.05);

        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 4f, 0.5f);
        world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 3f, 0.4f);
        world.createExplosion(location, 4f, false, true);

        for (Player player : new ArrayList<>(arena.getAlive())) {
            double dist = player.getLocation().distance(location);
            if (dist < 7.0) {
                double damage = Math.max(8.0 * (1.0 - dist / 7.0), 1.0);
                player.damage(damage);

                Vector knockback = player.getLocation().toVector()
                    .subtract(location.toVector())
                    .normalize()
                    .multiply(1.5 * (1.0 - dist / 7.0));
                knockback.setY(0.5);
                player.setVelocity(player.getVelocity().add(knockback));
            }
        }

        final List<Location> fireBlocks = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (ThreadLocalRandom.current().nextInt(3) == 0) continue;
                int fireY = world.getHighestBlockYAt(
                    location.getBlockX() + dx, location.getBlockZ() + dz
                );
                Location fireLoc = new Location(world,
                    location.getBlockX() + dx,
                    fireY + 1.0,
                    location.getBlockZ() + dz
                );
                if (fireLoc.getBlock().getType() == Material.AIR) {
                    fireLoc.getBlock().setType(Material.FIRE);
                    fireBlocks.add(fireLoc);
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : fireBlocks) {
                    if (loc.getBlock().getType() == Material.FIRE) {
                        loc.getBlock().setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(Disasters.getInstance(), 60L);

        for (int i = 0; i <= 6; i++) {
            Material[] debrisMats = {
                Material.COBBLESTONE, Material.NETHERRACK,
                Material.MAGMA_BLOCK, Material.BLACKSTONE
            };
            Material debrisMat = debrisMats[ThreadLocalRandom.current().nextInt(debrisMats.length)];

            FallingBlock debris = world.spawn(
                location.clone().add(0.0, 2.0, 0.0),
                FallingBlock.class
            );
            debris.setBlockData(debrisMat.createBlockData());
            debris.setDropItem(false);
            debris.setHurtEntities(true);
            debris.setDamagePerBlock(2f);
            debris.setMaxDamage(6);
            debris.setVelocity(new Vector(
                ThreadLocalRandom.current().nextDouble(-0.8, 0.8),
                ThreadLocalRandom.current().nextDouble(0.5, 1.2),
                ThreadLocalRandom.current().nextDouble(-0.8, 0.8)
            ));
        }
    }

    @Override
    public void stop(Arena arena) {
        List<MeteorTracker> meteors = activeMeteors.get(arena);
        if (meteors != null) {
            for (MeteorTracker m : meteors) {
                if (!m.entity.isDead()) m.entity.remove();
            }
        }
        activeMeteors.remove(arena);
        arenas.remove(arena);
    }
}
