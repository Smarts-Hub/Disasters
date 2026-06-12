package me.hhitt.disasters.game.modification;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.DisasterSettings;
import me.hhitt.disasters.game.modification.impl.OneHeartModification;
import me.hhitt.disasters.game.modification.impl.SwapperModification;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class GameModificationRegistry {

    private static final List<GameModificationDefinition> DEFINITIONS;
    static {
        final List<GameModificationDefinition> defs = new ArrayList<>();
        final List<String> oneHeartLore = new ArrayList<>();
        oneHeartLore.add("Everyone has one heart.");
        oneHeartLore.add("No regen allowed.");
        defs.add(new GameModificationDefinition(
            "one-heart",
            "One Heart",
            Material.RED_DYE,
            oneHeartLore,
            OneHeartModification::new
        ));
        final List<String> swapperLore = new ArrayList<>();
        swapperLore.add("Players swap positions");
        swapperLore.add("during the match.");
        defs.add(new GameModificationDefinition(
            "swapper",
            "Swapper",
            Material.ENDER_PEARL,
            swapperLore,
            SwapperModification::new
        ));
        DEFINITIONS = Collections.unmodifiableList(defs);
    }

    private GameModificationRegistry() {
    }

    public static List<GameModificationDefinition> all() {
        return DEFINITIONS;
    }

    public static List<GameModificationDefinition> enabledDefinitions(final Arena arena) {
        final List<GameModificationDefinition> enabled = new ArrayList<>();
        for (final GameModificationDefinition def : DEFINITIONS) {
            if (DisasterSettings.isGameModificationEnabled(arena, def.getId())) {
                enabled.add(def);
            }
        }
        return enabled;
    }

    public static void start(final Arena arena, final Collection<String> ids) {
        stop(arena);
        final List<GameModificationDefinition> enabled = enabledDefinitions(arena);
        for (final GameModificationDefinition def : enabled) {
            if (ids.contains(def.getId())) {
                final GameModification mod = def.factory();
                mod.start(arena);
                arena.getActiveGameModifications().add(mod);
            }
        }
    }

    public static void pulse(final Arena arena, final int time) {
        final List<GameModification> active = new ArrayList<GameModification>(arena.getActiveGameModifications());
        for (final GameModification mod : active) {
            mod.pulse(arena, time);
        }
    }

    public static void stop(final Arena arena) {
        final List<GameModification> active = new ArrayList<GameModification>(arena.getActiveGameModifications());
        for (final GameModification mod : active) {
            mod.stop(arena);
        }
        arena.getActiveGameModifications().clear();
    }

    public static boolean isActive(final Arena arena, final String id) {
        for (final GameModification mod : arena.getActiveGameModifications()) {
            if (mod.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> displayNames(final Arena arena) {
        final List<String> names = new ArrayList<>();
        for (final GameModification mod : arena.getActiveGameModifications()) {
            names.add(mod.getDisplayName());
        }
        return names;
    }
}
