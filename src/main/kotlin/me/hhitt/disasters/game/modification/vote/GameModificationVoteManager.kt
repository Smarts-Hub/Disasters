package me.hhitt.disasters.game.modification.vote

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.modification.GameModificationDefinition
import me.hhitt.disasters.game.modification.GameModificationRegistry
import me.hhitt.disasters.util.Msg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class GameModificationVoteManager(private val arena: Arena) : Listener {

    private val votes = ConcurrentHashMap<UUID, String>()
    private val openInventories = ConcurrentHashMap<UUID, Inventory>()
    private var resolved = false
    private var enabled: List<GameModificationDefinition> = emptyList()

    fun startVote() {
        enabled = GameModificationRegistry.enabledDefinitions(arena)
        if (enabled.isEmpty()) {
            resolved = true
            return
        }
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance())
        arena.playing.toList().forEach { player -> openGui(player) }
    }

    private fun openGui(player: Player) {
        val size = when {
            enabled.size <= 9 -> 9
            enabled.size <= 18 -> 18
            else -> 27
        }
        val titleRaw = Disasters.getInstance().config.getString("game-modifications.voting.inventory-title", "<gold><bold>Vote Game Modification")
        val title: Component = try {
            Msg.parse(titleRaw!!, player)
        } catch (_: Exception) {
            Component.text("Vote Game Modification")
        }
        val inventory = Bukkit.createInventory(null, size, title)
        val slotOffset = (size - enabled.size) / 2
        enabled.forEachIndexed { idx, def ->
            val item = ItemStack(def.material).apply {
                editMeta { meta ->
                    meta.displayName(
                        Component.text(def.displayName)
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD)
                            .decoration(TextDecoration.ITALIC, false)
                    )
                    meta.lore(def.description.map {
                        Component.text(it)
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
                    })
                }
            }
            inventory.setItem(slotOffset + idx, item)
        }
        openInventories[player.uniqueId] = inventory
        player.openInventory(inventory)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = openInventories[player.uniqueId] ?: return
        if (event.inventory != inventory) return
        event.isCancelled = true
        val slot = event.rawSlot
        val size = inventory.size
        val slotOffset = (size - enabled.size) / 2
        val idx = slot - slotOffset
        if (idx < 0 || idx >= enabled.size) return
        val def = enabled[idx]
        votes[player.uniqueId] = def.id
        player.closeInventory()
        player.sendMessage(
            Component.text("You voted for ").color(NamedTextColor.GRAY)
                .append(Component.text(def.displayName).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
        )
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        openInventories.remove(player.uniqueId)
    }

    fun resolveVote(): List<String> {
        if (resolved) return emptyList()
        resolved = true

        openInventories.forEach { (uuid, _) ->
            Bukkit.getPlayer(uuid)?.closeInventory()
        }
        openInventories.clear()
        HandlerList.unregisterAll(this)

        if (enabled.isEmpty()) return emptyList()

        val allowDefault = Disasters.getInstance().config.getBoolean("game-modifications.voting.allow-no-vote-default", true)
        val winner: String = if (votes.isNotEmpty()) {
            votes.values
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
                ?: enabled.first().id
        } else if (allowDefault) {
            val defaultId = Disasters.getInstance().config.getString("game-modifications.voting.default", enabled.first().id)
            if (enabled.any { it.id == defaultId }) defaultId!! else enabled.first().id
        } else {
            enabled.first().id
        }
        votes.clear()
        return listOf(winner)
    }
}
