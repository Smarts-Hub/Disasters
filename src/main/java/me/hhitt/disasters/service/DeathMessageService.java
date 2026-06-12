package me.hhitt.disasters.service;

import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DeathMessageService {

    private static final Map<UUID, Mark> marks = new ConcurrentHashMap<>();

    private DeathMessageService() {
    }

    public static void mark(Player player, String disasterId) {
        marks.put(player.getUniqueId(), new Mark(disasterId, System.currentTimeMillis()));
    }

    public static Component messageFor(Player player) {
        return messageFor(player, "default");
    }

    public static Component messageFor(Player player, String fallbackDisasterId) {
        long now = System.currentTimeMillis();
        Mark mark = marks.get(player.getUniqueId());
        String id;
        if (mark != null && now - mark.timestampMs <= 10_000) {
            id = mark.disasterId;
        } else {
            id = fallbackDisasterId;
        }
        Configuration config = FileManager.get("deadmessages");
        String raw = null;
        if (config != null) {
            raw = config.getString("messages." + id);
            if (raw == null) {
                raw = config.getString("messages.default");
            }
        }
        if (raw == null) {
            raw = player.getName() + " died.";
        }
        String resolved = raw.replace("%player%", player.getName()).replace("%disaster%", id);
        return Msg.parse(resolved, player);
    }

    public static void clear(Player player) {
        marks.remove(player.getUniqueId());
    }

    private static final class Mark {
        final String disasterId;
        final long timestampMs;

        Mark(String disasterId, long timestampMs) {
            this.disasterId = disasterId;
            this.timestampMs = timestampMs;
        }
    }
}
