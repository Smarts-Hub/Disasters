package me.hhitt.disasters.game.drop;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.model.drop.ItemDrop;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Msg;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ItemDropManager {

    public static final class ItemEntry {
        public final Material material;
        public final int amount;
        public final PotionType potionType;

        public ItemEntry(Material material, int amount, PotionType potionType) {
            this.material = material;
            this.amount = amount;
            this.potionType = potionType;
        }

        public ItemEntry(Material material, int amount) {
            this(material, amount, null);
        }

        public ItemEntry(Material material) {
            this(material, 1, null);
        }
    }

    public static final class ItemTier {
        public final String name;
        public final int chance;
        public final String color;
        public final List<ItemEntry> items;

        public ItemTier(String name, int chance, String color, List<ItemEntry> items) {
            this.name = name;
            this.chance = chance;
            this.color = color;
            this.items = items;
        }
    }

    public static final NamespacedKey DROP_KEY = new NamespacedKey(Disasters.getInstance(), "disaster_drop");
    public static final NamespacedKey SPECIAL_DROP_KEY = new NamespacedKey(Disasters.getInstance(), "special_drop");

    private static final ConcurrentHashMap<Arena, CopyOnWriteArrayList<ItemDrop>> activeDrops = new ConcurrentHashMap<Arena, CopyOnWriteArrayList<ItemDrop>>();

    private static boolean enabled = true;
    private static int dropRate = 20;
    private static int maxDropsPerCycle = 10;
    private static int despawnTime = 30;
    private static int spawnRadius = 15;
    private static boolean glow = true;
    private static final List<ItemTier> tiers = new ArrayList<ItemTier>();

    private static final Random RANDOM = new Random();

    private ItemDropManager() {
    }

    public static void loadConfig() {
        Configuration mainConfig = FileManager.get("config");
        boolean globalEnabled = mainConfig != null && mainConfig.getBoolean("enable-item-drops", true);

        Configuration config = FileManager.get("item-drops");
        if (config == null) {
            enabled = false;
            return;
        }

        enabled = globalEnabled && config.getBoolean("enabled", true);
        dropRate = config.getInt("drop-rate", 20);
        maxDropsPerCycle = config.getInt("max-players-per-cycle", 10);
        despawnTime = config.getInt("despawn-time", 30);
        spawnRadius = config.getInt("spawn-radius", 15);
        glow = config.getBoolean("glow", true);

        tiers.clear();
        ConfigurationSection tiersSection = config.getConfigurationSection("tiers");
        if (tiersSection != null) {
            for (String tierName : tiersSection.getKeys(false)) {
                ConfigurationSection tierSection = tiersSection.getConfigurationSection(tierName);
                if (tierSection == null) {
                    continue;
                }
                int chance = tierSection.getInt("chance", 0);
                String color = tierSection.getString("color", "<white>");
                List<String> rawItems = tierSection.getStringList("items");
                List<ItemEntry> items = new ArrayList<ItemEntry>();
                for (String raw : rawItems) {
                    ItemEntry entry = parseItemEntry(raw);
                    if (entry != null) {
                        items.add(entry);
                    }
                }
                if (!items.isEmpty()) {
                    tiers.add(new ItemTier(tierName, chance, color, items));
                }
            }
        }

        if (enabled && tiers.isEmpty()) {
            Disasters.getInstance().getLogger().warning("Item drops enabled but no valid tiers configured!");
        }
    }

    private static ItemEntry parseItemEntry(String raw) {
        String[] parts = raw.split(":");
        String materialName = parts[0].toUpperCase(Locale.ROOT);
        Material material = Material.matchMaterial(materialName);

        if (material == null) {
            Disasters.getInstance().getLogger().warning("Invalid item drop material: " + materialName);
            return null;
        }

        if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION) {
            PotionType potionType = null;
            if (parts.length > 1) {
                try {
                    potionType = parsePotionType(parts[1].toUpperCase(Locale.ROOT));
                } catch (Exception e) {
                    Disasters.getInstance().getLogger().warning("Invalid potion type: " + parts[1]);
                }
            }
            return new ItemEntry(material, 1, potionType);
        }

        int amount = 1;
        if (parts.length > 1) {
            try {
                amount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                amount = 1;
            }
        }
        return new ItemEntry(material, amount);
    }

    private static PotionType parsePotionType(String name) {
        if ("INSTANT_HEAL".equals(name) || "HEALING".equals(name)) {
            return PotionType.HEALING;
        } else if ("FIRE_RESISTANCE".equals(name)) {
            return PotionType.FIRE_RESISTANCE;
        } else if ("REGENERATION".equals(name)) {
            return PotionType.REGENERATION;
        } else if ("SWIFTNESS".equals(name) || "SPEED".equals(name)) {
            return PotionType.SWIFTNESS;
        } else if ("SLOW_FALLING".equals(name)) {
            return PotionType.SLOW_FALLING;
        } else if ("RESISTANCE".equals(name)) {
            try {
                return PotionType.valueOf("STRONG_TURTLE_MASTER");
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            try {
                return PotionType.valueOf(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private static ItemTier pickRandomTier() {
        if (tiers.isEmpty()) {
            return null;
        }
        int totalWeight = 0;
        for (ItemTier tier : tiers) {
            totalWeight += tier.chance;
        }
        if (totalWeight <= 0) {
            return tiers.get(RANDOM.nextInt(tiers.size()));
        }
        int roll = RANDOM.nextInt(totalWeight);
        for (ItemTier tier : tiers) {
            roll -= tier.chance;
            if (roll < 0) {
                return tier;
            }
        }
        return tiers.get(tiers.size() - 1);
    }

    public static void pulse(Arena arena, int gameTime) {
        if (!enabled || tiers.isEmpty()) {
            return;
        }

        CopyOnWriteArrayList<ItemDrop> drops = activeDrops.computeIfAbsent(arena, k -> new CopyOnWriteArrayList<ItemDrop>());

        java.util.Iterator<ItemDrop> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemDrop drop = iterator.next();
            if (gameTime - drop.getSpawnTime() >= despawnTime || drop.getItem().isDead()) {
                drop.remove();
                drops.remove(drop);
            }
        }

        if (gameTime > 0 && gameTime % dropRate == 0) {
            List<Player> players = new ArrayList<Player>(arena.getAlive());
            Collections.shuffle(players, RANDOM);
            int count = Math.min(maxDropsPerCycle, players.size());
            for (int i = 0; i < count; i++) {
                spawnDrop(arena, gameTime, players.get(i));
            }
        }
    }

    private static void spawnDrop(Arena arena, int gameTime, Player player) {
        Location location = getRandomSurfaceLocation(arena, player);
        if (location == null) {
            return;
        }
        ItemTier tier = pickRandomTier();
        if (tier == null) {
            return;
        }
        ItemEntry entry = tier.items.get(RANDOM.nextInt(tier.items.size()));
        ItemStack itemStack = createItemStack(entry);
        String displayName = getDisplayName(entry);

        World world = location.getWorld();
        if (world == null) {
            return;
        }

        Item item = world.dropItem(location, itemStack);
        item.setVelocity(new Vector(0, 0, 0));
        item.setUnlimitedLifetime(true);
        item.setCustomNameVisible(false);
        item.getPersistentDataContainer().set(DROP_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setCanPlayerPickup(true);
        item.setCanMobPickup(false);
        if (glow) {
            item.setGlowing(true);
        }

        Location labelLoc = location.clone().add(0.0, 1.2, 0.0);
        ArmorStand label = world.spawn(labelLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setCustomNameVisible(true);
            stand.customName(Msg.parse(tier.color + displayName));
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setSmall(true);
            stand.setInvulnerable(true);
            stand.setCanTick(false);
            stand.getPersistentDataContainer().set(DROP_KEY, PersistentDataType.BYTE, (byte) 1);
        });

        ItemDrop drop = new ItemDrop(item, label, gameTime);
        activeDrops.computeIfAbsent(arena, k -> new CopyOnWriteArrayList<ItemDrop>()).add(drop);
    }

    private static ItemStack createItemStack(ItemEntry entry) {
        ItemStack stack = new ItemStack(entry.material, entry.amount);
        if (entry.potionType != null) {
            if (stack.getItemMeta() instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta) stack.getItemMeta();
                meta.setBasePotionType(entry.potionType);
                stack.setItemMeta(meta);
            }
        }
        if (stack.getItemMeta() != null) {
            org.bukkit.inventory.meta.ItemMeta meta = stack.getItemMeta();
            if (entry.material == Material.TNT) {
                meta.setDisplayName(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(Msg.parse("<red>Throwable TNT")));
                meta.getPersistentDataContainer().set(SPECIAL_DROP_KEY, PersistentDataType.BYTE, (byte) 1);
            } else if (entry.material == Material.WATER_BUCKET || entry.material == Material.MILK_BUCKET) {
                meta.getPersistentDataContainer().set(SPECIAL_DROP_KEY, PersistentDataType.BYTE, (byte) 1);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static String getDisplayName(ItemEntry entry) {
        if (entry.potionType != null) {
            String potionName = entry.potionType.name().replace("_", " ").toLowerCase(Locale.ROOT);
            potionName = potionName.substring(0, 1).toUpperCase(Locale.ROOT) + potionName.substring(1);
            String typeName;
            if (entry.material == Material.SPLASH_POTION) {
                typeName = "Splash";
            } else if (entry.material == Material.LINGERING_POTION) {
                typeName = "Lingering";
            } else {
                typeName = "";
            }
            String prefix = !typeName.isEmpty() ? typeName + " " : "";
            return prefix + "Potion of " + potionName;
        }
        if (entry.amount > 1) {
            String name = entry.material.name().replace("_", " ").toLowerCase(Locale.ROOT);
            name = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
            return name + " x" + entry.amount;
        }
        String name = entry.material.name().replace("_", " ").toLowerCase(Locale.ROOT);
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    private static Location getRandomSurfaceLocation(Arena arena, Player player) {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();

        for (int i = 0; i < 10; i++) {
            int offsetX = RANDOM.nextInt(spawnRadius * 2 + 1) - spawnRadius;
            int offsetZ = RANDOM.nextInt(spawnRadius * 2 + 1) - spawnRadius;
            int x = playerLoc.getBlockX() + offsetX;
            int z = playerLoc.getBlockZ() + offsetZ;

            Location checkLoc = new Location(world, (double) x, playerLoc.getY(), (double) z);
            if (!arena.getBorderService().isLocationInArena(checkLoc)) {
                continue;
            }

            Double surfaceY = findSurface(world, x, z, playerLoc.getBlockY() - 10, playerLoc.getBlockY() + 10);
            if (surfaceY != null) {
                return new Location(world, x + 0.5, surfaceY + 1.0, z + 0.5);
            }
        }
        return null;
    }

    private static Double findSurface(World world, int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            org.bukkit.block.Block block = world.getBlockAt(x, y, z);
            org.bukkit.block.Block above = world.getBlockAt(x, y + 1, z);
            if (block.getType().isSolid() && above.getType().isAir()) {
                return (double) y;
            }
        }
        return null;
    }

    public static void onPickup(Arena arena, Item item) {
        CopyOnWriteArrayList<ItemDrop> drops = activeDrops.get(arena);
        if (drops == null) {
            return;
        }
        for (ItemDrop drop : drops) {
            if (drop.getItem().equals(item)) {
                drop.getLabel().remove();
                drops.remove(drop);
                return;
            }
        }
    }

    public static void clearDrops(Arena arena) {
        CopyOnWriteArrayList<ItemDrop> drops = activeDrops.remove(arena);
        if (drops != null) {
            for (ItemDrop drop : drops) {
                drop.remove();
            }
        }
    }

    public static void clearAll() {
        for (CopyOnWriteArrayList<ItemDrop> drops : activeDrops.values()) {
            for (ItemDrop drop : drops) {
                drop.remove();
            }
        }
        activeDrops.clear();
    }

    public static void cleanOrphanedEntities(Arena arena) {
        World world = arena.getCorner1().getWorld();
        if (world == null) {
            return;
        }
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity.getPersistentDataContainer().has(DROP_KEY, PersistentDataType.BYTE)) {
                if (arena.getBorderService().isLocationInArena(entity.getLocation())) {
                    entity.remove();
                }
            }
        }
    }
}
