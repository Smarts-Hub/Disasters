package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import me.hhitt.disasters.util.SpawnLocationFinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Apocalypse implements Disaster {

    private enum ZombieVariant {
        NORMAL(55), SPEED(20), BOOMER(10), GIANT(5), JUMPER(10);

        private final int weight;

        ZombieVariant(int weight) {
            this.weight = weight;
        }

        int getWeight() {
            return weight;
        }
    }

    private static final int MAX_ALIVE = 100;
    private static final int MIN_SPAWN_DISTANCE = 10;
    private static final int SPAWN_RADIUS = 15;
    private static final int STUCK_CHECK_INTERVAL = 5;
    private static final int Y_VARIATION = 5;

    private final CopyOnWriteArrayList<Arena> arenas = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Arena, ConcurrentHashMap<Zombie, Location>> lastLocations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Arena, Set<UUID>> boomers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Arena, Set<UUID>> jumpers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Arena, Integer> startTimes = new ConcurrentHashMap<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        startTimes.put(arena, 0);
        lastLocations.put(arena, new ConcurrentHashMap<Zombie, Location>());
        boomers.put(arena, ConcurrentHashMap.<UUID>newKeySet());
        jumpers.put(arena, ConcurrentHashMap.<UUID>newKeySet());
        Notify.disaster(arena, "apocalypse");
    }

    @Override
    public void pulse(int time) {
        for (Arena arena : arenas) {
            int elapsed = startTimes.getOrDefault(arena, 0) + 1;
            startTimes.put(arena, elapsed);

            boolean shouldSpawn;
            if (elapsed <= 20) {
                shouldSpawn = elapsed % 2 == 0;
            } else if (elapsed <= 120) {
                shouldSpawn = elapsed % 5 == 0;
            } else {
                shouldSpawn = elapsed % 60 == 0;
            }

            if (shouldSpawn) {
                ConcurrentHashMap<Zombie, Location> zombieMap = lastLocations.get(arena);
                if (zombieMap == null) continue;
                int aliveCount = 0;
                for (Zombie z : zombieMap.keySet()) {
                    if (!z.isDead()) aliveCount++;
                }
                if (aliveCount < MAX_ALIVE) {
                    for (Player player : arena.getAlive()) {
                        spawnZombiesNearPlayer(arena, player, 2);
                    }
                }
            }

            Set<UUID> jumperSet = jumpers.get(arena);
            if (jumperSet != null) {
                for (UUID id : new ArrayList<>(jumperSet)) {
                    org.bukkit.entity.Entity entity = Bukkit.getEntity(id);
                    if (entity == null || !(entity instanceof Zombie) || entity.isDead()) {
                        jumperSet.remove(id);
                        continue;
                    }
                    Zombie zombie = (Zombie) entity;
                    Player target = null;
                    double minDist = Double.MAX_VALUE;
                    for (Player p : arena.getAlive()) {
                        double d = p.getLocation().distanceSquared(zombie.getLocation());
                        if (d < minDist) {
                            minDist = d;
                            target = p;
                        }
                    }
                    if (target == null) continue;
                    double dx = target.getLocation().getX() - zombie.getLocation().getX();
                    double dz = target.getLocation().getZ() - zombie.getLocation().getZ();
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist < 12.0 && elapsed % 5 == 0) {
                        Vector vel = zombie.getVelocity();
                        vel.setX((dx / dist) * 0.4);
                        vel.setZ((dz / dist) * 0.4);
                        vel.setY(1.0);
                        zombie.setVelocity(vel);
                    }
                }
            }

            Set<UUID> boomerSet = boomers.get(arena);
            if (boomerSet == null) continue;
            for (UUID id : new ArrayList<>(boomerSet)) {
                org.bukkit.entity.Entity entity = Bukkit.getEntity(id);
                if (entity == null || !(entity instanceof Zombie) || entity.isDead()) {
                    if (entity != null) {
                        Location loc = entity.getLocation();
                        for (Player p : arena.getAlive()) {
                            if (p.getLocation().distanceSquared(loc) <= 16.0) {
                                DeathMessageService.mark(p, "apocalypse");
                            }
                        }
                        loc.getWorld().createExplosion(loc, 2f, false, true);
                    }
                    boomerSet.remove(id);
                }
            }

            if (time % STUCK_CHECK_INTERVAL == 0) {
                ConcurrentHashMap<Zombie, Location> zombieMap = lastLocations.get(arena);
                if (zombieMap == null) continue;
                List<Zombie> deadZombies = new ArrayList<>();

                for (Map.Entry<Zombie, Location> entry : zombieMap.entrySet()) {
                    Zombie zombie = entry.getKey();
                    Location lastLoc = entry.getValue();
                    if (zombie.isDead()) {
                        deadZombies.add(zombie);
                        continue;
                    }
                    Location currentLoc = zombie.getLocation();
                    if (lastLoc.distanceSquared(currentLoc) < 1.0) {
                        Player nearestPlayer = null;
                        double minDist = Double.MAX_VALUE;
                        for (Player p : arena.getAlive()) {
                            double d = p.getLocation().distanceSquared(zombie.getLocation());
                            if (d < minDist) {
                                minDist = d;
                                nearestPlayer = p;
                            }
                        }
                        if (nearestPlayer != null) {
                            breakBlocksTowardTarget(zombie, nearestPlayer.getLocation());
                        }
                    }
                    zombieMap.put(zombie, currentLoc);
                }
                for (Zombie z : deadZombies) {
                    zombieMap.remove(z);
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        arenas.remove(arena);
        startTimes.remove(arena);
        boomers.remove(arena);
        jumpers.remove(arena);
        ConcurrentHashMap<Zombie, Location> removed = lastLocations.remove(arena);
        if (removed != null) {
            for (Zombie zombie : removed.keySet()) {
                if (!zombie.isDead()) zombie.remove();
            }
        }
    }

    private void spawnZombiesNearPlayer(Arena arena, Player player, int amount) {
        World world = player.getWorld();
        ConcurrentHashMap<Zombie, Location> zombieMap = lastLocations.get(arena);
        if (zombieMap == null) return;
        Set<UUID> boomerSet = boomers.get(arena);
        if (boomerSet == null) return;
        Set<UUID> jumperSet = jumpers.get(arena);
        if (jumperSet == null) return;

        for (int i = 0; i < amount; i++) {
            Location spawnLocation = SpawnLocationFinder.findNearPlayer(arena, player.getLocation(), MIN_SPAWN_DISTANCE, SPAWN_RADIUS, Y_VARIATION);
            Zombie zombie = (Zombie) world.spawnEntity(spawnLocation, EntityType.ZOMBIE);

            ZombieVariant variant = pickVariant();
            switch (variant) {
                case NORMAL:
                    if (ThreadLocalRandom.current().nextInt(20) == 0) zombie.setBaby();
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        if (zombie.getEquipment() != null) {
                            zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                        }
                    }
                    break;
                case SPEED:
                    if (ThreadLocalRandom.current().nextInt(20) == 0) zombie.setBaby();
                    if (zombie.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                        double base = zombie.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
                        zombie.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(base * 1.6);
                    }
                    if (zombie.getEquipment() != null) {
                        zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                    }
                    break;
                case BOOMER:
                    if (zombie.getEquipment() != null) {
                        zombie.getEquipment().setHelmet(new ItemStack(Material.TNT));
                    }
                    boomerSet.add(zombie.getUniqueId());
                    break;
                case GIANT:
                    if (zombie.getAttribute(Attribute.SCALE) != null) {
                        zombie.getAttribute(Attribute.SCALE).setBaseValue(2.0);
                    }
                    break;
                case JUMPER:
                    jumperSet.add(zombie.getUniqueId());
                    break;
            }

            zombieMap.put(zombie, zombie.getLocation());
        }
    }

    private ZombieVariant pickVariant() {
        int total = 0;
        for (ZombieVariant v : ZombieVariant.values()) {
            total += v.getWeight();
        }
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (ZombieVariant v : ZombieVariant.values()) {
            roll -= v.getWeight();
            if (roll < 0) return v;
        }
        return ZombieVariant.NORMAL;
    }

    private void breakBlocksTowardTarget(Zombie zombie, Location target) {
        Location zombieLoc = zombie.getLocation();
        Vector direction = target.toVector().subtract(zombieLoc.toVector()).normalize();

        for (int dist = 1; dist <= 2; dist++) {
            int checkX = (int) (zombieLoc.getX() + direction.getX() * dist);
            int checkZ = (int) (zombieLoc.getZ() + direction.getZ() * dist);
            for (int yOffset = 0; yOffset <= 1; yOffset++) {
                int checkY = zombieLoc.getBlockY() + yOffset;
                Block block = zombieLoc.getWorld().getBlockAt(checkX, checkY, checkZ);
                if (block.getType() != Material.AIR && !block.isLiquid()) {
                    block.setType(Material.AIR);
                }
            }
        }
    }
}
