package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class Freeze implements Disaster, TriggerTrackedDisaster, Listener {

    private int triggerCount = 0;

    private final List<Arena> arenas = new ArrayList<>();

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        Notify.disaster(arena, "freeze");
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
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.setTo(event.getFrom());
            triggerCount++;
        }
    }
}
