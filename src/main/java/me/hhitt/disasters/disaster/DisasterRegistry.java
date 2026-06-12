package me.hhitt.disasters.disaster;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.impl.AcidRain;
import me.hhitt.disasters.disaster.impl.AnvilRain;
import me.hhitt.disasters.disaster.impl.Apocalypse;
import me.hhitt.disasters.disaster.impl.BatSwarm;
import me.hhitt.disasters.disaster.impl.Blind;
import me.hhitt.disasters.disaster.impl.BlockDisappear;
import me.hhitt.disasters.disaster.impl.Cobweb;
import me.hhitt.disasters.disaster.impl.Covid19;
import me.hhitt.disasters.disaster.impl.Disco;
import me.hhitt.disasters.disaster.impl.ExplosiveSheep;
import me.hhitt.disasters.disaster.impl.FloorIsLava;
import me.hhitt.disasters.disaster.impl.Flood;
import me.hhitt.disasters.disaster.impl.Freeze;
import me.hhitt.disasters.disaster.impl.Grounded;
import me.hhitt.disasters.disaster.impl.HotPotato;
import me.hhitt.disasters.disaster.impl.HotSun;
import me.hhitt.disasters.disaster.impl.Lag;
import me.hhitt.disasters.disaster.impl.Landmine;
import me.hhitt.disasters.disaster.impl.LavaRising;
import me.hhitt.disasters.disaster.impl.Lightning;
import me.hhitt.disasters.disaster.impl.MeteorShower;
import me.hhitt.disasters.disaster.impl.MirrorControls;
import me.hhitt.disasters.disaster.impl.Nuke;
import me.hhitt.disasters.disaster.impl.PillagerInvasion;
import me.hhitt.disasters.disaster.impl.RedLightGreenLight;
import me.hhitt.disasters.disaster.impl.SimonSays;
import me.hhitt.disasters.disaster.impl.Sinkhole;
import me.hhitt.disasters.disaster.impl.SizeChange;
import me.hhitt.disasters.disaster.impl.TntRain;
import me.hhitt.disasters.disaster.impl.Tornado;
import me.hhitt.disasters.disaster.impl.Wither;
import me.hhitt.disasters.disaster.impl.WorldBorder;
import me.hhitt.disasters.disaster.impl.ZeroGravity;
import me.hhitt.disasters.model.block.DisappearBlock;
import me.hhitt.disasters.model.block.DisasterFloor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DisasterRegistry {

    private static final ConcurrentHashMap<Arena, CopyOnWriteArrayList<ActiveDisaster>> activeDisasters = new ConcurrentHashMap<>();

    private static final List<DisasterDefinition> DEFINITIONS = buildDefinitions();

    private static final Map<String, DisasterDefinition> DEFINITIONS_BY_ID = buildDefinitionsById(DEFINITIONS);

    private static final Set<String> DEFINITION_IDS = Collections.unmodifiableSet(new LinkedHashSet<String>(DEFINITIONS_BY_ID.keySet()));

    private static final Set<Arena> noDisasterWarned = Collections.synchronizedSet(new HashSet<Arena>());

    private static final Random RANDOM = new Random();

    private static List<DisasterDefinition> buildDefinitions() {
        final List<DisasterDefinition> definitions = new ArrayList<DisasterDefinition>();
        definitions.add(new DisasterDefinition("acid-rain", "Acid Rain", AcidRain.class, AcidRain::new));
        definitions.add(new DisasterDefinition("apocalypse", "Zombie Apocalypse", Apocalypse.class, Apocalypse::new));
        definitions.add(new DisasterDefinition("explosive-sheep", "Explosive Sheep", ExplosiveSheep.class, ExplosiveSheep::new));
        definitions.add(new DisasterDefinition("floor-is-lava", "Floor Is Lava", FloorIsLava.class, FloorIsLava::new));
        definitions.add(new DisasterDefinition("grounded", "Grounded", Grounded.class, Grounded::new));
        definitions.add(new DisasterDefinition("lightning", "Lightning", Lightning.class, Lightning::new));
        definitions.add(new DisasterDefinition("world-border", "World Border", WorldBorder.class, WorldBorder::new));
        definitions.add(new DisasterDefinition("blind", "Blind", Blind.class, Blind::new));
        definitions.add(new DisasterDefinition("cobweb", "Cobweb", Cobweb.class, Cobweb::new));
        definitions.add(new DisasterDefinition("lag", "Lag", Lag.class, Lag::new));
        definitions.add(new DisasterDefinition("zero-gravity", "Zero Gravity", ZeroGravity.class, ZeroGravity::new));
        definitions.add(new DisasterDefinition("wither", "Wither", Wither.class, Wither::new));
        definitions.add(new DisasterDefinition("hot-sun", "Hot Sun", HotSun.class, HotSun::new));
        definitions.add(new DisasterDefinition("disappear-blocks", "Disappear Blocks", BlockDisappear.class, BlockDisappear::new));
        definitions.add(new DisasterDefinition("meteor-shower", "Meteor Shower", MeteorShower.class, MeteorShower::new));
        definitions.add(new DisasterDefinition("flood", "Flood", Flood.class, Flood::new));
        definitions.add(new DisasterDefinition("lava-rising", "Lava Rising", LavaRising.class, LavaRising::new, Collections.<String>singleton("flood")));
        definitions.add(new DisasterDefinition("tnt-rain", "TNT Rain", TntRain.class, TntRain::new));
        definitions.add(new DisasterDefinition("anvil-rain", "Anvil Rain", AnvilRain.class, AnvilRain::new));
        definitions.add(new DisasterDefinition("tornado", "Tornado", Tornado.class, Tornado::new));
        definitions.add(new DisasterDefinition("simon-says", "Simon Says", SimonSays.class, SimonSays::new));
        definitions.add(new DisasterDefinition("hot-potato", "Hot Potato", HotPotato.class, HotPotato::new));
        definitions.add(new DisasterDefinition("sinkhole", "Sinkhole", Sinkhole.class, Sinkhole::new));
        definitions.add(new DisasterDefinition("pillager-invasion", "Pillager Invasion", PillagerInvasion.class, PillagerInvasion::new));
        definitions.add(new DisasterDefinition("freeze", "Freeze", Freeze.class, Freeze::new));
        definitions.add(new DisasterDefinition("landmine", "Landmine", Landmine.class, Landmine::new));
        definitions.add(new DisasterDefinition("size-change", "Size Change", SizeChange.class, SizeChange::new));
        definitions.add(new DisasterDefinition("bat-swarm", "Bat Swarm", BatSwarm.class, BatSwarm::new));
        definitions.add(new DisasterDefinition("nuke", "Nuke", Nuke.class, Nuke::new));
        definitions.add(new DisasterDefinition("covid-19", "Covid-19", Covid19.class, Covid19::new));
        definitions.add(new DisasterDefinition("disco", "Disco", Disco.class, Disco::new, Collections.<String>singleton("red-light-green-light")));
        definitions.add(new DisasterDefinition("red-light-green-light", "Red Light Green Light", RedLightGreenLight.class, RedLightGreenLight::new, Collections.<String>singleton("disco")));
        definitions.add(new DisasterDefinition("mirror-controls", "Mirror Controls", MirrorControls.class, MirrorControls::new));
        return Collections.unmodifiableList(definitions);
    }

    private static Map<String, DisasterDefinition> buildDefinitionsById(final List<DisasterDefinition> definitions) {
        final LinkedHashMap<String, DisasterDefinition> byId = new LinkedHashMap<String, DisasterDefinition>();
        for (final DisasterDefinition definition : definitions) {
            final String id = normalizeId(definition.getId());
            final DisasterDefinition duplicate = byId.put(id, definition);
            if (duplicate != null) {
                throw new IllegalStateException("Duplicate disaster definition id: " + id);
            }
        }
        for (final DisasterDefinition definition : definitions) {
            for (final String incompatibleId : definition.getIncompatibleWith()) {
                final String normalizedId = normalizeId(incompatibleId);
                if (!byId.containsKey(normalizedId)) {
                    throw new IllegalStateException("Disaster definition " + definition.getId() + " references unknown incompatible id: " + incompatibleId);
                }
            }
        }
        return Collections.unmodifiableMap(byId);
    }

    private static String normalizeId(final String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }

    private DisasterRegistry() {
    }

    private static <T extends Disaster> T getDisaster(Arena arena, Class<T> type) {
        List<ActiveDisaster> list = activeDisasters.get(arena);
        if (list == null) {
            return null;
        }
        for (ActiveDisaster wrapper : list) {
            Disaster disaster = wrapper.getDisaster();
            if (type.isInstance(disaster)) {
                return type.cast(disaster);
            }
        }
        return null;
    }

    public static List<DisasterDefinition> allDefinitions() {
        return DEFINITIONS;
    }

    public static Set<String> definitionIds() {
        return DEFINITION_IDS;
    }

    public static Optional<DisasterDefinition> findDefinition(final String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(DEFINITIONS_BY_ID.get(normalizeId(id)));
    }

    public static void addRandomDisaster(Arena arena) {
        int maxDisasters = DisasterSettings.maxSimultaneousDisasters(arena);
        CopyOnWriteArrayList<ActiveDisaster> currentDisasters = activeDisasters.computeIfAbsent(arena, k -> new CopyOnWriteArrayList<ActiveDisaster>());

        if (currentDisasters.size() >= maxDisasters) {
            ActiveDisaster toRemove = currentDisasters.remove(0);
            toRemove.getDisaster().stop(arena);
            arena.getDisasters().remove(toRemove.getDisaster());
        }

        Set<String> activeIds = new HashSet<String>();
        Set<String> activeIncompatibilities = new HashSet<String>();
        for (ActiveDisaster ad : currentDisasters) {
            activeIds.add(ad.getDefinition().getId());
            activeIncompatibilities.addAll(ad.getDefinition().getIncompatibleWith());
        }

        List<DisasterDefinition> available = new ArrayList<DisasterDefinition>();
        for (DisasterDefinition def : DEFINITIONS) {
            if (!activeIds.contains(def.getId())
                && DisasterSettings.isDisasterEnabled(arena, def.getId())
                && Collections.disjoint(def.getIncompatibleWith(), activeIds)
                && !activeIncompatibilities.contains(def.getId())) {
                available.add(def);
            }
        }

        if (available.isEmpty()) {
            if (noDisasterWarned.add(arena)) {
                Disasters.getInstance().getLogger().warning("No enabled compatible disaster for arena " + arena.getName() + ".");
            }
            return;
        }

        DisasterDefinition definition = available.get(RANDOM.nextInt(available.size()));
        Disaster disaster = definition.factory();
        disaster.start(arena);
        ActiveDisaster active = new ActiveDisaster(
            definition,
            disaster,
            DisasterSettings.durationSeconds(definition.getId()),
            DisasterSettings.maxTriggers(definition.getId())
        );
        currentDisasters.add(active);
        arena.getDisasters().add(disaster);
    }

    public static void pulseAll() {
        for (Map.Entry<Arena, CopyOnWriteArrayList<ActiveDisaster>> entry : activeDisasters.entrySet()) {
            final Arena arena = entry.getKey();
            final CopyOnWriteArrayList<ActiveDisaster> list = entry.getValue();
            for (final ActiveDisaster wrapper : new ArrayList<ActiveDisaster>(list)) {
                if (pulseOne(arena, wrapper)) {
                    list.remove(wrapper);
                }
            }
            if (list.isEmpty()) {
                activeDisasters.remove(arena, list);
            }
        }
    }

    static boolean pulseOne(final Arena arena, final ActiveDisaster wrapper) {
        final int elapsed = wrapper.advanceSecond();
        try {
            wrapper.getDisaster().pulse(elapsed);
        } catch (Exception e) {
            Disasters.getInstance().getLogger().log(Level.SEVERE, "Error pulsing disaster " + wrapper.getDefinition().getId() + " in arena " + arena.getName() + ": " + e.getMessage(), e);
        }

        final int triggerCap = wrapper.getMaxTriggers();
        final boolean triggered = wrapper.getDisaster() instanceof TriggerTrackedDisaster
            && ((TriggerTrackedDisaster) wrapper.getDisaster()).getTriggerCount() >= triggerCap
            && triggerCap > 0;
        final boolean durationReached = elapsed >= wrapper.getDurationSeconds();

        if (durationReached || triggered) {
            try {
                wrapper.getDisaster().stop(arena);
            } catch (Exception e) {
                Disasters.getInstance().getLogger().log(Level.SEVERE, "Error stopping disaster " + wrapper.getDefinition().getId() + " in arena " + arena.getName() + ": " + e.getMessage(), e);
            }
            arena.getDisasters().remove(wrapper.getDisaster());
            return true;
        }
        return false;
    }

    public static void removeDisasters(Arena arena) {
        CopyOnWriteArrayList<ActiveDisaster> removed = activeDisasters.remove(arena);
        if (removed != null) {
            for (ActiveDisaster wrapper : removed) {
                wrapper.getDisaster().stop(arena);
                arena.getDisasters().remove(wrapper.getDisaster());
            }
        }
        arena.getDisasters().clear();
        noDisasterWarned.remove(arena);
    }

    public static List<String> getActiveDisasterNames(Arena arena) {
        List<ActiveDisaster> list = activeDisasters.get(arena);
        if (list == null) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<String>();
        for (ActiveDisaster wrapper : list) {
            names.add(wrapper.getDefinition().getDisplayName());
        }
        return names;
    }

    public static boolean hasDisaster(Arena arena, String id) {
        List<ActiveDisaster> list = activeDisasters.get(arena);
        if (list == null) {
            return false;
        }
        for (ActiveDisaster wrapper : list) {
            if (wrapper.getDefinition().getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPlayerOnClimbable(Location location) {
        return Tag.CLIMBABLE.isTagged(location.getBlock().getType());
    }

    private static Location getBlockUnderPlayer(Location location) {
        if (location.getY() % 1.0 != 0.0) {
            Location atFeet = location.clone();
            if (!atFeet.getBlock().getType().isAir() && atFeet.getBlock().getType().isSolid()) {
                return atFeet;
            }
        }
        Location blockBelow = location.clone().subtract(0.0, 1.0, 0.0);
        if (!blockBelow.getBlock().getType().isAir()) {
            return blockBelow;
        }
        return null;
    }

    public static void addBlockToDisappear(Arena arena, Location location) {
        if (isPlayerOnClimbable(location)) {
            return;
        }
        Location block = getBlockUnderPlayer(location);
        if (block == null) {
            return;
        }
        BlockDisappear disaster = getDisaster(arena, BlockDisappear.class);
        if (disaster != null) {
            disaster.addBlock(arena, block);
        }
    }

    public static void removeBlockFromDisappear(Arena arena, DisappearBlock block) {
        BlockDisappear disaster = getDisaster(arena, BlockDisappear.class);
        if (disaster != null) {
            disaster.removeBlock(block);
        }
    }

    public static void setBlockUnoccupied(Arena arena, Location location) {
        BlockDisappear disaster = getDisaster(arena, BlockDisappear.class);
        if (disaster != null) {
            disaster.setUnoccupied(location);
        }
    }

    public static void addBlockToFloorIsLava(Arena arena, Location location) {
        if (isPlayerOnClimbable(location)) {
            return;
        }
        Location block = getBlockUnderPlayer(location);
        if (block == null) {
            return;
        }
        Material blockType = block.getBlock().getType();
        if (blockType == Material.WATER || blockType == Material.LAVA) {
            return;
        }
        DisasterFloor floorBlock = new DisasterFloor(arena, block);
        FloorIsLava disaster = getDisaster(arena, FloorIsLava.class);
        if (disaster != null) {
            disaster.addBlock(floorBlock);
        }
    }

    public static void removeBlockFromFloorIsLava(Arena arena, DisasterFloor block) {
        FloorIsLava disaster = getDisaster(arena, FloorIsLava.class);
        if (disaster != null) {
            disaster.removeBlock(block);
        }
    }

    public static boolean isGrounded(Arena arena, Player player) {
        Grounded disaster = getDisaster(arena, Grounded.class);
        return disaster != null && disaster.isGrounded(player);
    }
}
