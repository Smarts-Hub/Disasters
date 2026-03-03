package me.hhitt.disasters.game.vote

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameMode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
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

/**
 * Manages the PvP vs PvE vote and disaster multiplier vote during countdown.
 * Two-phase voting: first game mode, then disaster multiplier.
 */
class VoteManager(private val arena: Arena) : Listener {

    private val modeVotes = ConcurrentHashMap<UUID, GameMode>()
    private val multiplierVotes = ConcurrentHashMap<UUID, Int>()
    private val openInventories = ConcurrentHashMap<UUID, Inventory>()
    private var resolved = false
    private var phase = VotePhase.MODE

    private enum class VotePhase { MODE, MULTIPLIER }

    companion object {
        private const val PVP_SLOT = 2
        private const val PVE_SLOT = 6
        private const val INV_SIZE = 9

        private val MODE_TITLE = Component.text("Vote: PvP or PvE")
            .color(NamedTextColor.DARK_PURPLE)
            .decorate(TextDecoration.BOLD)

        private val MULTIPLIER_TITLE = Component.text("Vote: Disaster Multiplier")
            .color(NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD)

        // Multiplier slots: 1x=0, 2x=2, 3x=4, 4x=6, 5x=8
        private val MULTIPLIER_SLOTS = mapOf(0 to 1, 2 to 2, 4 to 3, 6 to 4, 8 to 5)
    }

    /**
     * Opens the mode vote GUI for all players.
     */
    fun startVote() {
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance())
        phase = VotePhase.MODE
        arena.playing.forEach { player -> openModeGUI(player) }
    }

    private fun openModeGUI(player: Player) {
        val inventory = Bukkit.createInventory(null, INV_SIZE, MODE_TITLE)

        val pvpItem = ItemStack(Material.DIAMOND_SWORD).apply {
            editMeta { meta ->
                meta.displayName(
                    Component.text("PvP Disasters")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)
                )
                meta.lore(listOf(
                    Component.text("All disasters including")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                    Component.text("player-vs-player combat")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                ))
            }
        }

        val pveItem = ItemStack(Material.SHIELD).apply {
            editMeta { meta ->
                meta.displayName(
                    Component.text("PvE Disasters")
                        .color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)
                )
                meta.lore(listOf(
                    Component.text("Environmental disasters only")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                    Component.text("No player-vs-player combat")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                ))
            }
        }

        inventory.setItem(PVP_SLOT, pvpItem)
        inventory.setItem(PVE_SLOT, pveItem)

        openInventories[player.uniqueId] = inventory
        player.openInventory(inventory)
    }

    private fun openMultiplierGUI(player: Player) {
        val inventory = Bukkit.createInventory(null, INV_SIZE, MULTIPLIER_TITLE)

        MULTIPLIER_SLOTS.forEach { (slot, multiplier) ->
            val item = ItemStack(Material.BLAZE_POWDER, multiplier).apply {
                editMeta { meta ->
                    meta.displayName(
                        Component.text("${multiplier}x Disasters")
                            .color(when(multiplier) {
                                1 -> NamedTextColor.GREEN
                                2 -> NamedTextColor.YELLOW
                                3 -> NamedTextColor.GOLD
                                4 -> NamedTextColor.RED
                                5 -> NamedTextColor.DARK_RED
                                else -> NamedTextColor.WHITE
                            })
                            .decorate(TextDecoration.BOLD)
                            .decoration(TextDecoration.ITALIC, false)
                    )
                    meta.lore(listOf(
                        Component.text("Max ${arena.maxDisasters * multiplier} disasters at once")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
                    ))
                }
            }
            inventory.setItem(slot, item)
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

        when (phase) {
            VotePhase.MODE -> {
                val mode = when (event.rawSlot) {
                    PVP_SLOT -> GameMode.PVP
                    PVE_SLOT -> GameMode.PVE
                    else -> return
                }

                modeVotes[player.uniqueId] = mode
                player.closeInventory()

                val voteText = if (mode == GameMode.PVP) {
                    Component.text("You voted for ").color(NamedTextColor.GRAY)
                        .append(Component.text("PvP").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
                } else {
                    Component.text("You voted for ").color(NamedTextColor.GRAY)
                        .append(Component.text("PvE").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                }
                player.sendMessage(voteText)

                // Check if voting is enabled for multiplier
                val config = Disasters.getInstance().config
                if (config.getBoolean("voting.multiplier-enabled", true)) {
                    phase = VotePhase.MULTIPLIER
                    if (!resolved) {
                        openMultiplierGUI(player)
                    }
                }
            }
            VotePhase.MULTIPLIER -> {
                val multiplier = MULTIPLIER_SLOTS[event.rawSlot] ?: return

                multiplierVotes[player.uniqueId] = multiplier
                player.closeInventory()

                val color = when(multiplier) {
                    1 -> NamedTextColor.GREEN
                    2 -> NamedTextColor.YELLOW
                    3 -> NamedTextColor.GOLD
                    4 -> NamedTextColor.RED
                    5 -> NamedTextColor.DARK_RED
                    else -> NamedTextColor.WHITE
                }
                player.sendMessage(
                    Component.text("You voted for ").color(NamedTextColor.GRAY)
                        .append(Component.text("${multiplier}x").color(color).decorate(TextDecoration.BOLD))
                        .append(Component.text(" disasters").color(NamedTextColor.GRAY))
                )
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        openInventories.remove(player.uniqueId)
    }

    /**
     * Resolves both votes. Returns the winning GameMode.
     * Also sets arena.disasterMultiplier based on multiplier vote.
     * Ties default to PVP for mode and 1x for multiplier.
     */
    fun resolveVote(): GameMode {
        if (resolved) return arena.gameMode
        resolved = true

        // Close any still-open inventories
        openInventories.forEach { (uuid, _) ->
            Bukkit.getPlayer(uuid)?.closeInventory()
        }
        openInventories.clear()

        HandlerList.unregisterAll(this)

        // Resolve game mode
        val pvpVotes = modeVotes.values.count { it == GameMode.PVP }
        val pveVotes = modeVotes.values.count { it == GameMode.PVE }
        val modeResult = if (pveVotes > pvpVotes) GameMode.PVE else GameMode.PVP

        // Resolve multiplier (most voted value wins, tie goes to lowest)
        val multiplierResult = if (multiplierVotes.isNotEmpty()) {
            multiplierVotes.values
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: 1
        } else {
            1
        }

        arena.disasterMultiplier = multiplierResult

        // Announce mode result
        val modeText = if (modeResult == GameMode.PVP) {
            Component.text("Vote result: ").color(NamedTextColor.GOLD)
                .append(Component.text("PvP Disasters!").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
        } else {
            Component.text("Vote result: ").color(NamedTextColor.GOLD)
                .append(Component.text("PvE Disasters!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
        }
        arena.playing.forEach { it.sendMessage(modeText) }

        // Announce multiplier result if voting was enabled
        val config = Disasters.getInstance().config
        if (config.getBoolean("voting.multiplier-enabled", true)) {
            val multColor = when(multiplierResult) {
                1 -> NamedTextColor.GREEN
                2 -> NamedTextColor.YELLOW
                3 -> NamedTextColor.GOLD
                4 -> NamedTextColor.RED
                5 -> NamedTextColor.DARK_RED
                else -> NamedTextColor.WHITE
            }
            val multText = Component.text("Disaster intensity: ").color(NamedTextColor.GOLD)
                .append(Component.text("${multiplierResult}x").color(multColor).decorate(TextDecoration.BOLD))
                .append(Component.text(" (max ${arena.maxDisasters * multiplierResult})").color(NamedTextColor.GRAY))
            arena.playing.forEach { it.sendMessage(multText) }
        }

        modeVotes.clear()
        multiplierVotes.clear()
        return modeResult
    }
}