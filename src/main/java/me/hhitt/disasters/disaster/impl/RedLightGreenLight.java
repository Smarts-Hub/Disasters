package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Msg;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class RedLightGreenLight implements Disaster, Listener {

    private final List<Arena> arenas = new ArrayList<>();
    private boolean phaseRed = false;
    private int lastPhaseChange = 0;

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        phaseRed = false;
        lastPhaseChange = 0;
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        Notify.disaster(arena, "red-light-green-light");
    }

    @Override
    public void pulse(int time) {
        if (time - lastPhaseChange >= 4) {
            phaseRed = !phaseRed;
            lastPhaseChange = time;
            String title = phaseRed ? "<red><bold>RED LIGHT" : "<green><bold>GREEN LIGHT";
            for (Arena arena : new ArrayList<>(arenas)) {
                for (org.bukkit.entity.Player p : arena.getAlive()) {
                    Msg.sendTitle(p, title);
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
        if (!phaseRed) return;
        Arena arena = null;
        for (Arena a : arenas) {
            if (a.isPlayerValid(event.getPlayer())) {
                arena = a;
                break;
            }
        }
        if (arena == null) return;
        double dx = event.getTo().getX() - event.getFrom().getX();
        double dy = event.getTo().getY() - event.getFrom().getY();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        if (dx * dx + dy * dy + dz * dz > 0.0009) {
            event.setCancelled(true);
            DeathMessageService.mark(event.getPlayer(), "red-light-green-light");
            event.getPlayer().getWorld().strikeLightningEffect(event.getTo());
            event.getPlayer().damage(20.0);
        }
    }
}
