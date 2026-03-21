package me.hhitt.disasters.game.drop

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.model.drop.ItemDrop
import me.hhitt.disasters.util.Msg
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

/**
 * Manages item drop spawning, tracking, and cleanup for all arenas.
 */
object ItemDropManager {

    val DROP_KEY by lazy { NamespacedKey(Disasters.getInstance(), "disaster_drop") }

    private val activeDrops = ConcurrentHashMap<Arena, CopyOnWriteArrayList<ItemDrop>>()

    private var enabled = true
    private var dropRate = 20
    private var maxDropsPerCycle = 10
    private var despawnTime = 30
    private var spawnRadius = 15
    private var glow = true
    private var tiers = mutableListOf<ItemTier>()

    /**
     * Represents a parsed item entry from config.
     */
    data class ItemEntry(
        val material: Material,
        val amount: Int = 1,
        val potionType: PotionType? = null
    )

    /**
     * Represents a rarity tier with a weighted chance, display color, and item pool.
     */
    data class ItemTier(
        val name: String,
        val chance: Int,
        val color: String,
        val items: List<ItemEntry>
    )

    fun loadConfig() {
        // Global toggle from config.yml
        val mainConfig = me.hhitt.disasters.storage.file.FileManager.get("config")
        val globalEnabled = mainConfig?.getBoolean("enable-item-drops", true) ?: true

        val config = Disasters.getInstance().let { plugin ->
            val file = java.io.File(plugin.dataFolder, "item-drops.yml")
            if (!file.exists()) {
                plugin.saveResource("item-drops.yml", false)
            }
            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file)
        }

        enabled = globalEnabled && config.getBoolean("enabled", true)
        dropRate = config.getInt("drop-rate", 20)
        maxDropsPerCycle = config.getInt("max-players-per-cycle", 10)
        despawnTime = config.getInt("despawn-time", 30)
        spawnRadius = config.getInt("spawn-radius", 15)
        glow = config.getBoolean("glow", true)

        tiers.clear()
        val tiersSection = config.getConfigurationSection("tiers")
        if (tiersSection != null) {
            for (tierName in tiersSection.getKeys(false)) {
                val tierSection = tiersSection.getConfigurationSection(tierName) ?: continue
                val chance = tierSection.getInt("chance", 0)
                val color = tierSection.getString("color", "<white>") ?: "<white>"
                val rawItems = tierSection.getStringList("items")
                val items = rawItems.mapNotNull { parseItemEntry(it) }
                if (items.isNotEmpty()) {
                    tiers.add(ItemTier(tierName, chance, color, items))
                }
            }
        }

        if (enabled && tiers.isEmpty()) {
            Disasters.getInstance().logger.warning("Item drops enabled but no valid tiers configured!")
        }
    }

    private fun parseItemEntry(raw: String): ItemEntry? {
        // Formats: MATERIAL, MATERIAL:AMOUNT, POTION:EFFECT, SPLASH_POTION:EFFECT
        val parts = raw.split(":")
        val materialName = parts[0].uppercase()
        val material = Material.matchMaterial(materialName)

        if (material == null) {
            Disasters.getInstance().logger.warning("Invalid item drop material: $materialName")
            return null
        }

        // Potion types
        if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION) {
            val potionType = if (parts.size > 1) {
                try {
                    parsePotionType(parts[1].uppercase())
                } catch (e: Exception) {
                    Disasters.getInstance().logger.warning("Invalid potion type: ${parts[1]}")
                    null
                }
            } else null
            return ItemEntry(material, 1, potionType)
        }

        // Regular items with optional amount
        val amount = if (parts.size > 1) parts[1].toIntOrNull() ?: 1 else 1
        return ItemEntry(material, amount)
    }

    private fun parsePotionType(name: String): PotionType? {
        return when (name) {
            "INSTANT_HEAL", "HEALING" -> PotionType.HEALING
            "FIRE_RESISTANCE" -> PotionType.FIRE_RESISTANCE
            "REGENERATION" -> PotionType.REGENERATION
            "SWIFTNESS", "SPEED" -> PotionType.SWIFTNESS
            "SLOW_FALLING" -> PotionType.SLOW_FALLING
            "RESISTANCE" -> {
                try { PotionType.valueOf("STRONG_TURTLE_MASTER") } catch (_: Exception) { null }
            }
            else -> try { PotionType.valueOf(name) } catch (_: Exception) { null }
        }
    }

    /**
     * Picks a random tier based on configured chances (weighted random).
     */
    private fun pickRandomTier(): ItemTier? {
        if (tiers.isEmpty()) return null
        val totalWeight = tiers.sumOf { it.chance }
        if (totalWeight <= 0) return tiers.random()
        var roll = Random.nextInt(totalWeight)
        for (tier in tiers) {
            roll -= tier.chance
            if (roll < 0) return tier
        }
        return tiers.last()
    }

    /**
     * Called every second from GameTimer. Handles spawning and despawning.
     */
    fun pulse(arena: Arena, gameTime: Int) {
        if (!enabled || tiers.isEmpty()) return

        val drops = activeDrops.getOrPut(arena) { CopyOnWriteArrayList() }

        // Despawn expired drops
        val iterator = drops.iterator()
        while (iterator.hasNext()) {
            val drop = iterator.next()
            if (gameTime - drop.spawnTime >= despawnTime || drop.item.isDead) {
                drop.remove()
                drops.remove(drop)
            }
        }

        // Spawn a drop near each alive player (up to max-players-per-cycle)
        if (gameTime > 0 && gameTime % dropRate == 0) {
            val players = arena.alive.shuffled().take(maxDropsPerCycle)
            for (player in players) {
                spawnDrop(arena, gameTime, player)
            }
        }
    }

    private fun spawnDrop(arena: Arena, gameTime: Int, player: org.bukkit.entity.Player) {
        val location = getRandomSurfaceLocation(arena, player) ?: return
        val tier = pickRandomTier() ?: return
        val entry = tier.items.random()
        val itemStack = createItemStack(entry)
        val displayName = getDisplayName(entry)

        val world = location.world ?: return

        // Spawn the item entity with no velocity so it stays in place
        val item = world.dropItem(location, itemStack)
        item.velocity = Vector(0, 0, 0)
        item.setUnlimitedLifetime(true)
        item.isCustomNameVisible = false
        item.persistentDataContainer.set(DROP_KEY, PersistentDataType.BYTE, 1.toByte())
        item.setCanPlayerPickup(true)
        item.setCanMobPickup(false)
        if (glow) item.isGlowing = true

        // Spawn floating text label above the item with tier color
        val label = world.spawn(location.clone().add(0.0, 1.2, 0.0), ArmorStand::class.java) { stand ->
            stand.isVisible = false
            stand.isCustomNameVisible = true
            stand.customName(Msg.parse("${tier.color}$displayName"))
            stand.setGravity(false)
            stand.isMarker = true
            stand.isSmall = true
            stand.isInvulnerable = true
            stand.setCanTick(false)
            stand.persistentDataContainer.set(DROP_KEY, PersistentDataType.BYTE, 1.toByte())
        }

        val drop = ItemDrop(item, label, gameTime)
        activeDrops.getOrPut(arena) { CopyOnWriteArrayList() }.add(drop)
    }

    private fun createItemStack(entry: ItemEntry): ItemStack {
        val stack = ItemStack(entry.material, entry.amount)
        if (entry.potionType != null) {
            val meta = stack.itemMeta as? PotionMeta
            if (meta != null) {
                meta.basePotionType = entry.potionType
                stack.itemMeta = meta
            }
        }
        return stack
    }

    private fun getDisplayName(entry: ItemEntry): String {
        if (entry.potionType != null) {
            val potionName = entry.potionType.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
            val typeName = when (entry.material) {
                Material.SPLASH_POTION -> "Splash"
                Material.LINGERING_POTION -> "Lingering"
                else -> ""
            }
            val prefix = if (typeName.isNotEmpty()) "$typeName " else ""
            return "${prefix}Potion of $potionName"
        }
        if (entry.amount > 1) {
            val name = entry.material.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
            return "$name x${entry.amount}"
        }
        return entry.material.name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    /**
     * Finds a surface spot near the given player within spawnRadius.
     */
    private fun getRandomSurfaceLocation(arena: Arena, player: org.bukkit.entity.Player): Location? {
        val world = player.world
        val playerLoc = player.location

        // Try a few times to find a valid surface spot near the player
        repeat(10) {
            val offsetX = Random.nextInt(-spawnRadius, spawnRadius + 1)
            val offsetZ = Random.nextInt(-spawnRadius, spawnRadius + 1)
            val x = playerLoc.blockX + offsetX
            val z = playerLoc.blockZ + offsetZ

            // Make sure the spot is within the arena
            val checkLoc = Location(world, x.toDouble(), playerLoc.y, z.toDouble())
            if (!arena.borderService.isLocationInArena(checkLoc)) return@repeat

            // Scan for surface near the player's Y level
            val surfaceY = findSurface(world, x, z, playerLoc.blockY - 10, playerLoc.blockY + 10)
            if (surfaceY != null) {
                return Location(world, x + 0.5, surfaceY + 1.0, z + 0.5)
            }
        }
        return null
    }

    /**
     * Scans downward from maxY to find the highest solid block with air above.
     */
    private fun findSurface(world: World, x: Int, z: Int, minY: Int, maxY: Int): Double? {
        for (y in maxY downTo minY) {
            val block = world.getBlockAt(x, y, z)
            val above = world.getBlockAt(x, y + 1, z)
            if (block.type.isSolid && above.type.isAir) {
                return y.toDouble()
            }
        }
        return null
    }

    /**
     * Called when a player picks up a disaster drop item.
     * Removes the associated label ArmorStand.
     */
    fun onPickup(arena: Arena, item: Item) {
        val drops = activeDrops[arena] ?: return
        val drop = drops.find { it.item == item } ?: return
        drop.label.remove()
        drops.remove(drop)
    }

    /**
     * Removes all active drops for an arena (called on game end).
     */
    fun clearDrops(arena: Arena) {
        val drops = activeDrops.remove(arena) ?: return
        drops.forEach { it.remove() }
    }

    /**
     * Removes all drops across all arenas (called on plugin disable).
     */
    fun clearAll() {
        activeDrops.forEach { (_, drops) -> drops.forEach { it.remove() } }
        activeDrops.clear()
    }

    /**
     * Removes any orphaned drop entities left in the arena (e.g. from a server crash).
     * Called when a new game starts in an arena.
     */
    fun cleanOrphanedEntities(arena: Arena) {
        val world = arena.corner1.world ?: return
        world.entities.forEach { entity ->
            if (entity.persistentDataContainer.has(DROP_KEY, PersistentDataType.BYTE)) {
                if (arena.borderService.isLocationInArena(entity.location)) {
                    entity.remove()
                }
            }
        }
    }
}
