package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.util.Msg;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SizeChange implements Disaster, TriggerTrackedDisaster {

    private int triggerCount;

    private final List<Arena> arenas = new ArrayList<>();
    private final Map<UUID, Double> oldScales = new ConcurrentHashMap<>();

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        for (Player player : new ArrayList<>(arena.getAlive())) {
            org.bukkit.attribute.AttributeInstance attr = player.getAttribute(Attribute.SCALE);
            if (attr == null) continue;
            oldScales.put(player.getUniqueId(), attr.getBaseValue());
            double newScale = ThreadLocalRandom.current().nextBoolean() ? 0.5 : 2.0;
            attr.setBaseValue(newScale);
            player.sendMessage(Msg.parse("<yellow>Size change: <white>" + newScale + "x"));
        }
        triggerCount = 1;
        Notify.disaster(arena, "size-change");
    }

    @Override
    public void pulse(int time) {
    }

    @Override
    public void stop(Arena arena) {
        arenas.remove(arena);
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : oldScales.entrySet()) {
            UUID uuid = entry.getKey();
            double scale = entry.getValue();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                org.bukkit.attribute.AttributeInstance attr = player.getAttribute(Attribute.SCALE);
                if (attr != null) {
                    attr.setBaseValue(scale);
                }
            }
            toRemove.add(uuid);
        }
        for (UUID uuid : toRemove) {
            oldScales.remove(uuid);
        }
    }

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }
}
