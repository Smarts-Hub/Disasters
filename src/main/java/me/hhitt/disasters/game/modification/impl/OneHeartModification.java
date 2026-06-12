package me.hhitt.disasters.game.modification.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.game.modification.GameModification;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OneHeartModification implements GameModification {

    private final Map<UUID, Double> oldMaxHealth = new ConcurrentHashMap<UUID, Double>();

    @Override
    public String getId() {
        return "one-heart";
    }

    @Override
    public String getDisplayName() {
        return "One Heart";
    }

    @Override
    public void start(final Arena arena) {
        final FileConfiguration config = FileManager.get("config");
        final double health = config != null ? config.getDouble("game-modifications.one-heart.health", 2.0) : 2.0;
        for (final Player player : new ArrayList<Player>(arena.getPlaying())) {
            final AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                oldMaxHealth.putIfAbsent(player.getUniqueId(), attr.getBaseValue());
                attr.setBaseValue(health);
                if (player.getHealth() > health) {
                    player.setHealth(health);
                }
                player.setAbsorptionAmount(0.0);
            }
        }
        Notify.disaster(arena, "one-heart");
    }

    @Override
    public void pulse(final Arena arena, final int time) {
        final FileConfiguration config = FileManager.get("config");
        final double health = config != null ? config.getDouble("game-modifications.one-heart.health", 2.0) : 2.0;
        for (final Player player : new ArrayList<Player>(arena.getAlive())) {
            if (player.getHealth() > health) {
                player.setHealth(health);
            }
            if (player.getAbsorptionAmount() > 0.0) {
                player.setAbsorptionAmount(0.0);
            }
        }
    }

    @Override
    public void stop(final Arena arena) {
        final List<UUID> toRemove = new ArrayList<UUID>();
        for (final Map.Entry<UUID, Double> entry : oldMaxHealth.entrySet()) {
            final UUID uuid = entry.getKey();
            final double value = entry.getValue();
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                final AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
                if (attr != null) {
                    attr.setBaseValue(value);
                    if (player.getHealth() > value) {
                        player.setHealth(value);
                    }
                }
            }
            toRemove.add(uuid);
        }
        for (final UUID uuid : toRemove) {
            oldMaxHealth.remove(uuid);
        }
    }
}
