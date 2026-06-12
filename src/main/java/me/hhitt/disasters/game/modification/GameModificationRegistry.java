package me.hhitt.disasters.game.modification;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.DisasterSettings;
import me.hhitt.disasters.game.modification.impl.OneHeartModification;
import me.hhitt.disasters.game.modification.impl.PvpModification;
import me.hhitt.disasters.game.modification.impl.SwapperModification;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class GameModificationRegistry {

    private static final List<GameModificationDefinition> DEFINITIONS;
    private static final Map<String, GameModificationDefinition> DEFINITIONS_BY_ID;
    private static final List<String> DEFINITION_IDS;

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
        final List<String> pvpLore = new ArrayList<>();
        pvpLore.add("Players can damage each other.");
        pvpLore.add("Only during the match.");
        defs.add(new GameModificationDefinition(
            "pvp",
            "PvP",
            Material.IRON_SWORD,
            pvpLore,
            PvpModification::new
        ));

        final LinkedHashMap<String, GameModificationDefinition> byId = new LinkedHashMap<String, GameModificationDefinition>();
        final List<String> ids = new ArrayList<String>();
        for (final GameModificationDefinition def : defs) {
            final String normalizedId = normalizeId(def.getId());
            if (normalizedId.isEmpty()) {
                throw new IllegalStateException("Game modification id must not be blank");
            }
            if (byId.containsKey(normalizedId)) {
                throw new IllegalStateException("Duplicate game modification id: " + def.getId());
            }
            byId.put(normalizedId, def);
            ids.add(normalizedId);
        }
        DEFINITIONS = Collections.unmodifiableList(defs);
        DEFINITIONS_BY_ID = Collections.unmodifiableMap(byId);
        DEFINITION_IDS = Collections.unmodifiableList(ids);
    }

    private GameModificationRegistry() {
    }

    public static List<GameModificationDefinition> all() {
        return DEFINITIONS;
    }

    public static Optional<GameModificationDefinition> find(final String id) {
        return Optional.ofNullable(DEFINITIONS_BY_ID.get(normalizeId(id)));
    }

    public static List<String> definitionIds() {
        return DEFINITION_IDS;
    }

    public static List<GameModificationDefinition> enabledDefinitions(final Arena arena) {
        final List<GameModificationDefinition> enabled = new ArrayList<GameModificationDefinition>();
        for (final GameModificationDefinition def : DEFINITIONS) {
            if (DisasterSettings.isGameModificationEnabled(arena, def.getId())) {
                enabled.add(def);
            }
        }
        return Collections.unmodifiableList(enabled);
    }

    public static void start(final Arena arena, final Collection<String> ids) {
        stop(arena);
        final LinkedHashSet<String> requestedIds = new LinkedHashSet<String>();
        for (final String id : ids) {
            final String normalizedId = normalizeId(id);
            if (!normalizedId.isEmpty()) {
                requestedIds.add(normalizedId);
            }
        }
        for (final String id : requestedIds) {
            final GameModificationDefinition def = DEFINITIONS_BY_ID.get(id);
            if (def == null) {
                warnUnknown(arena, id);
                continue;
            }
            if (!DisasterSettings.isGameModificationEnabled(arena, def.getId())) {
                continue;
            }
            final GameModification mod = def.factory();
            mod.start(arena);
            arena.getActiveGameModifications().add(mod);
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
        final String normalizedId = normalizeId(id);
        for (final GameModification mod : arena.getActiveGameModifications()) {
            if (normalizeId(mod.getId()).equals(normalizedId)) {
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

    private static String normalizeId(final String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private static void warnUnknown(final Arena arena, final String id) {
        final Disasters plugin = Disasters.getInstance();
        if (plugin != null) {
            plugin.getLogger().warning("Arena " + arena.getName() + " requested unknown game modification id: " + id);
        }
    }
}
