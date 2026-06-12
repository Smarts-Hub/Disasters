package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Disco implements Disaster, Listener {

    private final List<Arena> arenas = new ArrayList<>();
    private final ConcurrentHashMap<UUID, Long> lastMove = new ConcurrentHashMap<>();
    private static final List<Color> COLORS = Arrays.asList(
        Color.RED, Color.AQUA, Color.YELLOW, Color.LIME, Color.FUCHSIA, Color.ORANGE, Color.PURPLE
    );
    private static final Random RANDOM = new Random();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        for (Player player : arena.getAlive()) {
            lastMove.put(player.getUniqueId(), System.currentTimeMillis());
        }
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        Notify.disaster(arena, "disco");
    }

    @Override
    public void pulse(int time) {
        if (time % 2 != 0) return;
        long now = System.currentTimeMillis();
        for (Arena arena : new ArrayList<>(arenas)) {
            for (Player player : arena.getAlive()) {
                Color c = COLORS.get(RANDOM.nextInt(COLORS.size()));
                player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0.0, 1.0, 0.0), 6, 0.3, 0.3, 0.3, new Particle.DustOptions(c, 1.2f));
                if (time % 4 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.6f, 1.4f);
                }
                Long last = lastMove.get(player.getUniqueId());
                long lastTime = last != null ? last : now;
                if (now - lastTime > 1000) {
                    DeathMessageService.mark(player, "disco");
                    player.damage(2.0);
                    lastMove.put(player.getUniqueId(), now);
                }
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        arenas.remove(arena);
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        lastMove.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
