package me.hhitt.disasters.service;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;

public final class DefinitionToggleService {

    public void setDefaultDisaster(final String id, final boolean enabled) {
        set("default.disasters." + id, enabled);
    }

    public void setArenaDisaster(final Arena arena, final String id, final boolean enabled) {
        set("arenas." + arena.getName() + ".disasters." + id, enabled);
    }

    public void setDefaultModification(final String id, final boolean enabled) {
        set("default.game-modifications." + id, enabled);
    }

    public void setArenaModification(final Arena arena, final String id, final boolean enabled) {
        set("arenas." + arena.getName() + ".game-modifications." + id, enabled);
    }

    private void set(final String path, final boolean enabled) {
        final Configuration config = FileManager.get("enabledisasters");
        if (config == null) {
            throw new IllegalStateException("Configuration 'enabledisasters' is not loaded");
        }
        config.set(path, enabled);
        config.save();
    }
}
