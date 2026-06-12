package me.hhitt.disasters.disaster;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;

public final class DisasterSettings {

    private DisasterSettings() {
    }

    public static int maxSimultaneousDisasters(Arena arena) {
        Configuration config = FileManager.get("config");
        if (config == null) {
            return Math.max(arena.getMaxDisasters(), 1);
        }
        int global = config.getInt("disasters.max-simultaneous-disasters", arena.getMaxDisasters());
        if (arena.getMaxDisasters() > 0) {
            return arena.getMaxDisasters();
        }
        return Math.max(global, 1);
    }

    public static int durationSeconds(String id) {
        Configuration config = FileManager.get("config");
        if (config == null) {
            return 60;
        }
        return Math.max(config.getInt(
            "disasters.per-disaster." + id + ".duration-seconds",
            config.getInt("disasters.default-duration-seconds", 60)
        ), 1);
    }

    public static int maxTriggers(String id) {
        Configuration config = FileManager.get("config");
        if (config == null) {
            return 30;
        }
        return Math.max(config.getInt(
            "disasters.per-disaster." + id + ".max-triggers",
            config.getInt("disasters.default-max-triggers", 30)
        ), 0);
    }

    public static boolean isDisasterEnabled(Arena arena, String id) {
        Configuration config = FileManager.get("enabledisasters");
        if (config == null) {
            return true;
        }
        if (config.getStringList("default.blacklist.disasters").contains(id)) {
            return false;
        }
        if (config.getStringList("arenas." + arena.getName() + ".blacklist.disasters").contains(id)) {
            return false;
        }
        if (config.contains("arenas." + arena.getName() + ".disasters." + id)) {
            return config.getBoolean("arenas." + arena.getName() + ".disasters." + id);
        }
        return config.getBoolean("default.disasters." + id, true);
    }

    public static boolean isGameModificationEnabled(Arena arena, String id) {
        Configuration config = FileManager.get("enabledisasters");
        if (config == null) {
            return true;
        }
        if (config.getStringList("default.blacklist.game-modifications").contains(id)) {
            return false;
        }
        if (config.getStringList("arenas." + arena.getName() + ".blacklist.game-modifications").contains(id)) {
            return false;
        }
        if (config.contains("arenas." + arena.getName() + ".game-modifications." + id)) {
            return config.getBoolean("arenas." + arena.getName() + ".game-modifications." + id);
        }
        return config.getBoolean("default.game-modifications." + id, true);
    }
}
