package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MirrorControls implements Disaster, Listener {

    private final List<Arena> arenas = new ArrayList<>();
    private final Map<UUID, Long> guard = new ConcurrentHashMap<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        Notify.disaster(arena, "mirror-controls");
    }

    @Override
    public void pulse(int time) {
    }

    @Override
    public void stop(Arena arena) {
        arenas.remove(arena);
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Arena arena = null;
        for (Arena a : arenas) {
            if (a.isPlayerValid(event.getPlayer())) {
                arena = a;
                break;
            }
        }
        if (arena == null) return;
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;
        long now = System.currentTimeMillis();
        Long last = guard.get(event.getPlayer().getUniqueId());
        if (last != null && now - last < 50) return;
        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        if (dx == 0.0 && dz == 0.0) return;
        org.bukkit.Location inverted = event.getFrom().clone();
        inverted.setX(event.getFrom().getX() - dx);
        inverted.setZ(event.getFrom().getZ() - dz);
        inverted.setYaw(event.getTo().getYaw());
        inverted.setPitch(event.getTo().getPitch());
        inverted.setY(event.getTo().getY());
        event.setTo(inverted);
        guard.put(event.getPlayer().getUniqueId(), now);
    }
}
