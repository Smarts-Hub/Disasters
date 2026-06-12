package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class BatSwarm implements Disaster {

    private Arena arena;
    private final List<Bat> bats = new ArrayList<Bat>();
    private int initialCount;
    private int waveCount;
    private int waveInterval;
    private int maxAliveBats;
    private int spawnAttemptsPerBat;
    private double spawnHorizontalRadius;
    private double spawnMinYOffset;
    private double spawnMaxYOffset;
    private double effectRadiusSquared;
    private int blindnessDurationTicks;
    private int slownessDurationTicks;
    private int targetCursor;

    @Override
    public void start(Arena arena) {
        this.arena = arena;
        bats.clear();
        targetCursor = 0;
        loadSettings();
        spawnWave(initialCount);
        Notify.disaster(arena, "bat-swarm");
    }

    @Override
    public void pulse(int time) {
        if (arena == null) return;
        removeDeadBats();
        if (time > 0 && time % waveInterval == 0) {
            spawnWave(waveCount);
        }
        for (Player player : arena.getAlive()) {
            final Location playerLocation = player.getLocation();
            for (Bat bat : bats) {
                if (!isAliveBat(bat)) continue;
                final Location batLocation = bat.getLocation();
                if (batLocation.getWorld() != playerLocation.getWorld()) continue;
                if (batLocation.distanceSquared(playerLocation) <= effectRadiusSquared) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessDurationTicks, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessDurationTicks, 0, true, false));
                    DeathMessageService.mark(player, "bat-swarm");
                    break;
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        if (this.arena != arena) return;
        for (Bat bat : bats) {
            if (!bat.isDead()) bat.remove();
        }
        bats.clear();
        this.arena = null;
        targetCursor = 0;
    }

    private void spawnWave(int requested) {
        if (arena == null || requested <= 0) return;
        removeDeadBats();
        final List<Player> players = new ArrayList<Player>(arena.getAlive());
        if (players.isEmpty()) return;
        final int current = bats.size();
        if (current >= maxAliveBats) return;

        final int desired = Math.min(requested, maxAliveBats - current);
        int spawned = 0;
        for (int i = 0; i < desired; i++) {
            final Player player = players.get(targetCursor % players.size());
            targetCursor++;
            for (int attempt = 0; attempt < spawnAttemptsPerBat; attempt++) {
                final Location spawn = createSpawnLocation(player);
                if (!isValidSpawn(spawn)) continue;
                final Bat bat = spawn.getWorld().spawn(spawn, Bat.class);
                bats.add(bat);
                spawned++;
                break;
            }
        }
        if (spawned < desired) {
            Disasters.getInstance().getLogger().log(Level.WARNING,
                "Bat Swarm spawn shortfall in arena {0}: requested={1}, spawned={2}, current={3}, cap={4}, attempts={5}",
                new Object[]{arena.getName(), requested, spawned, bats.size(), maxAliveBats, spawnAttemptsPerBat});
        }
    }

    private void loadSettings() {
        final Configuration config = FileManager.get("config");
        initialCount = Math.max(getInt(config, "initial-count", 10), 0);
        waveCount = Math.max(getInt(config, "wave-count", 10), 0);
        waveInterval = Math.max(getInt(config, "wave-interval-seconds", 8), 1);
        maxAliveBats = Math.max(getInt(config, "max-alive-bats", 30), 1);
        spawnAttemptsPerBat = Math.min(Math.max(getInt(config, "spawn-attempts-per-bat", 12), 1), 100);
        spawnHorizontalRadius = Math.max(getDouble(config, "spawn-horizontal-radius", 4.0), 0.0);
        spawnMinYOffset = getDouble(config, "spawn-min-y-offset", 2.0);
        spawnMaxYOffset = Math.max(getDouble(config, "spawn-max-y-offset", 6.0), spawnMinYOffset);
        final double effectRadius = Math.max(getDouble(config, "effect-radius", 4.0), 0.0);
        effectRadiusSquared = effectRadius * effectRadius;
        blindnessDurationTicks = Math.max(getInt(config, "blindness-duration-ticks", 60), 1);
        slownessDurationTicks = Math.max(getInt(config, "slowness-duration-ticks", 60), 1);
    }

    private int getInt(Configuration config, String key, int fallback) {
        if (config == null) return fallback;
        return config.getInt("disasters.per-disaster.bat-swarm." + key, fallback);
    }

    private double getDouble(Configuration config, String key, double fallback) {
        if (config == null) return fallback;
        return config.getDouble("disasters.per-disaster.bat-swarm." + key, fallback);
    }

    private Location createSpawnLocation(Player player) {
        return player.getLocation().clone().add(
            randomOffset(spawnHorizontalRadius),
            randomBetween(spawnMinYOffset, spawnMaxYOffset),
            randomOffset(spawnHorizontalRadius)
        );
    }

    private boolean isValidSpawn(Location spawn) {
        if (arena == null) return false;
        final World world = arena.getCorner1().getWorld();
        if (world == null || spawn.getWorld() != world) return false;
        if (!arena.getBorderService().isLocationInArena(spawn)) return false;
        final Block block = spawn.getBlock();
        return block.isPassable() && block.getRelative(BlockFace.UP).isPassable();
    }

    private void removeDeadBats() {
        for (int i = bats.size() - 1; i >= 0; i--) {
            if (!isAliveBat(bats.get(i))) {
                bats.remove(i);
            }
        }
    }

    private boolean isAliveBat(Bat bat) {
        return bat != null && bat.isValid() && !bat.isDead();
    }

    private double randomOffset(double radius) {
        if (radius <= 0.0) return 0.0;
        return ThreadLocalRandom.current().nextDouble(-radius, radius);
    }

    private double randomBetween(double min, double max) {
        if (max <= min) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
