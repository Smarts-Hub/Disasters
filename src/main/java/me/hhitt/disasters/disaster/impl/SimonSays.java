package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SimonSays implements Disaster, TriggerTrackedDisaster, Listener {

    private int triggerCount;

    private enum Action {
        JUMP, SNEAK, CLICK
    }

    private final List<Arena> arenas = new ArrayList<>();
    private Action currentAction = Action.JUMP;
    private int currentDeadlineTick = -1;
    private int nextRoundTick = 0;
    private final Map<UUID, Boolean> completed = new ConcurrentHashMap<>();

    @Override
    public void start(Arena arena) {
        currentDeadlineTick = -1;
        nextRoundTick = 0;
        completed.clear();
        arenas.add(arena);
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        Notify.disaster(arena, "simon-says");
    }

    @Override
    public void pulse(int time) {
        if (currentDeadlineTick == -1) {
            startRound(time);
            return;
        }
        if (time >= currentDeadlineTick) {
            if (!arenas.isEmpty()) punish(arenas.get(0));
            startRound(time);
        }
    }

    private void startRound(int time) {
        if (arenas.isEmpty()) return;
        Action[] actions = Action.values();
        currentAction = actions[ThreadLocalRandom.current().nextInt(actions.length)];
        completed.clear();
        String title;
        switch (currentAction) {
            case JUMP:
                title = "<green><bold>JUMP";
                break;
            case SNEAK:
                title = "<green><bold>SNEAK";
                break;
            case CLICK:
                title = "<green><bold>CLICK";
                break;
            default:
                title = "";
        }
        for (org.bukkit.entity.Player p : new ArrayList<>(arenas.get(0).getAlive())) {
            Notify.playerMessageRaw(p, title);
        }
        currentDeadlineTick = time + 4;
        nextRoundTick = time + 6;
        triggerCount++;
    }

    private void punish(Arena arena) {
        for (org.bukkit.entity.Player player : new ArrayList<>(arena.getAlive())) {
            if (completed.get(player.getUniqueId()) != Boolean.TRUE) {
                DeathMessageService.mark(player, "simon-says");
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.damage(6.0);
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        arenas.remove(arena);
        completed.clear();
        HandlerList.unregisterAll(this);
    }

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (currentAction != Action.JUMP) return;
        if (event.getFrom().getY() < event.getTo().getY() && (event.getTo().getY() - event.getFrom().getY()) > 0.1) {
            completed.put(event.getPlayer().getUniqueId(), true);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (currentAction != Action.SNEAK) return;
        if (event.isSneaking()) {
            completed.put(event.getPlayer().getUniqueId(), true);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (currentAction != Action.CLICK) return;
        completed.put(event.getPlayer().getUniqueId(), true);
    }
}
