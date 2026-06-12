package me.hhitt.disasters.disaster

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.storage.file.FileManager

object DisasterSettings {

    fun maxSimultaneousDisasters(arena: Arena): Int {
        val config = FileManager.get("config") ?: return arena.maxDisasters.coerceAtLeast(1)
        val global = config.getInt("disasters.max-simultaneous-disasters", arena.maxDisasters)
        return arena.maxDisasters.takeIf { it > 0 } ?: global.coerceAtLeast(1)
    }

    fun durationSeconds(id: String): Int {
        val config = FileManager.get("config") ?: return 60
        return config.getInt(
            "disasters.per-disaster.$id.duration-seconds",
            config.getInt("disasters.default-duration-seconds", 60)
        ).coerceAtLeast(1)
    }

    fun maxTriggers(id: String): Int {
        val config = FileManager.get("config") ?: return 30
        return config.getInt(
            "disasters.per-disaster.$id.max-triggers",
            config.getInt("disasters.default-max-triggers", 30)
        ).coerceAtLeast(0)
    }

    fun isDisasterEnabled(arena: Arena, id: String): Boolean {
        val config = FileManager.get("enabledisasters") ?: return true
        if (config.getStringList("default.blacklist.disasters").contains(id)) return false
        if (config.getStringList("arenas.${arena.name}.blacklist.disasters").contains(id)) return false
        if (config.contains("arenas.${arena.name}.disasters.$id")) {
            return config.getBoolean("arenas.${arena.name}.disasters.$id")
        }
        return config.getBoolean("default.disasters.$id", true)
    }

    fun isGameModificationEnabled(arena: Arena, id: String): Boolean {
        val config = FileManager.get("enabledisasters") ?: return true
        if (config.getStringList("default.blacklist.game-modifications").contains(id)) return false
        if (config.getStringList("arenas.${arena.name}.blacklist.game-modifications").contains(id)) return false
        if (config.contains("arenas.${arena.name}.game-modifications.$id")) {
            return config.getBoolean("arenas.${arena.name}.game-modifications.$id")
        }
        return config.getBoolean("default.game-modifications.$id", true)
    }
}
