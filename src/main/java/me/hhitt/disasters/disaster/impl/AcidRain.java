package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class AcidRain implements Disaster {

    private final CopyOnWriteArrayList<Arena> arenas = new CopyOnWriteArrayList<>();

    @Override
    public void start(Arena arena) {
        for (Player player : arena.getPlaying()) {
            player.setPlayerWeather(WeatherType.DOWNFALL);
        }
        arenas.add(arena);
        Notify.disaster(arena, "acid-rain");
    }

    @Override
    public void pulse(int time) {
        for (Arena arena : arenas) {
            erodeArena(arena);

            for (Player player : arena.getAlive()) {
                Block block = player.getLocation().getBlock();

                if (isCovered(block)) {
                    if (ThreadLocalRandom.current().nextInt(5) == 0) {
                        World world = block.getWorld();
                        int highestY = world.getHighestBlockYAt(block.getX(), block.getZ());
                        Block topBlock = world.getBlockAt(block.getX(), highestY, block.getZ());
                        if (topBlock.getType() != Material.AIR && topBlock.getType().isSolid()) {
                            dissolveBlock(topBlock);
                        }
                    }
                } else {
                    player.damage(2.0);
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        for (Player player : arena.getPlaying()) {
            player.resetPlayerWeather();
        }
        arenas.remove(arena);
    }

    private void erodeArena(Arena arena) {
        for (Player player : arena.getAlive()) {
            Location loc = player.getLocation();
            World world = loc.getWorld();
            if (world == null) continue;

            int radius = 15;
            int blocksPerPlayer = 3;

            for (int i = 0; i < blocksPerPlayer; i++) {
                int offsetX = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
                int offsetZ = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
                int x = loc.getBlockX() + offsetX;
                int z = loc.getBlockZ() + offsetZ;

                int highestY = world.getHighestBlockYAt(x, z);
                Block topBlock = world.getBlockAt(x, highestY, z);

                if (topBlock.getType() != Material.AIR && topBlock.getType().isSolid()) {
                    dissolveBlock(topBlock);
                }
            }
        }
    }

    private boolean isCovered(Block block) {
        World world = block.getWorld();
        int highestY = world.getHighestBlockYAt(block.getX(), block.getZ());
        return highestY > block.getY();
    }

    private void dissolveBlock(Block block) {
        World world = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);

        block.setType(Material.AIR);

        DustOptions dust = new DustOptions(Color.fromRGB(80, 200, 50), 1.2f);
        world.spawnParticle(Particle.DUST, loc, 5, 0.4, 0.4, 0.4, dust);

        world.playSound(loc, Sound.BLOCK_SLIME_BLOCK_BREAK, 0.4f, 1.2f);
    }
}
