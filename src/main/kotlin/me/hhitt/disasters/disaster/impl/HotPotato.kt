package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class HotPotato : Disaster, Listener, TriggerTrackedDisaster {

    override var triggerCount: Int = 0
        private set

    private val arenas = mutableListOf<Arena>()
    private val holders = ConcurrentHashMap<Arena, UUID>()
    private val countdowns = ConcurrentHashMap<Arena, Int>()
    private val interval = 10

    companion object {
        fun makePotato(): ItemStack {
            val stack = ItemStack(Material.POTATO, 1)
            val meta: ItemMeta = stack.itemMeta
            meta.setDisplayName(ChatColor.RED.toString() + "Hot Potato")
            stack.itemMeta = meta
            return stack
        }

        fun isPotato(item: ItemStack?): Boolean {
            if (item == null || item.type != Material.POTATO) return false
            val meta = item.itemMeta ?: return false
            return meta.hasDisplayName() && meta.displayName.contains("Hot Potato")
        }
    }

    override fun start(arena: Arena) {
        arenas.add(arena)
        pickNewHolder(arena)
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance())
        Notify.disaster(arena, "hot-potato")
    }

    override fun pulse(time: Int) {
        arenas.toList().forEach { arena ->
            val holderId = holders[arena] ?: run {
                pickNewHolder(arena)
                return@forEach
            }
            val holder = Bukkit.getPlayer(holderId) ?: run {
                pickNewHolder(arena)
                return@forEach
            }
            if (!holder.inventory.containsAtLeast(ItemStack(Material.POTATO), 1)) {
                pickNewHolder(arena)
                return@forEach
            }
            val remain = (countdowns[arena] ?: interval) - 1
            countdowns[arena] = remain
            holder.sendActionBar("${ChatColor.RED}Hot Potato: ${remain}s")
            if (remain <= 0) {
                triggerCount++
                DeathMessageService.mark(holder, "hot-potato")
                holder.world.createExplosion(holder.location, 4f, false, true)
                if (holder.isOnline) {
                    holder.health = 0.0
                }
                removePotatoFromAll(arena)
                holders.remove(arena)
                countdowns.remove(arena)
                pickNewHolder(arena)
            }
        }
    }

    override fun stop(arena: Arena) {
        removePotatoFromAll(arena)
        holders.remove(arena)
        countdowns.remove(arena)
        arenas.remove(arena)
        HandlerList.unregisterAll(this)
    }

    private fun pickNewHolder(arena: Arena) {
        if (arena.alive.isEmpty()) return
        val newHolder = arena.alive.random()
        holders[arena] = newHolder.uniqueId
        countdowns[arena] = interval
        newHolder.inventory.addItem(makePotato())
    }

    private fun removePotatoFromAll(arena: Arena) {
        arena.playing.forEach { player ->
            val contents = player.inventory.contents
            for (i in contents.indices) {
                if (isPotato(contents[i])) {
                    player.inventory.setItem(i, null)
                }
            }
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val arena = arenas.firstOrNull { it.isPlayerValid(event.player) } ?: return
        if (event.player.uniqueId != holders[arena]) return
        val nearest = arena.alive.filter { it.uniqueId != event.player.uniqueId }
            .minByOrNull { it.location.distanceSquared(event.player.location) } ?: return
        if (event.player.location.distance(nearest.location) <= 1.5) {
            val hot = event.player.inventory.firstOrNull { isPotato(it) } ?: return
            event.player.inventory.removeItem(hot)
            nearest.inventory.addItem(hot.clone())
            holders[arena] = nearest.uniqueId
            countdowns[arena] = interval
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (isPotato(event.item)) {
            event.isCancelled = true
        }
    }
}
