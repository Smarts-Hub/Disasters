package me.hhitt.disasters.listener

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.util.Vector

class DisasterItemListener(private val arenaManager: ArenaManager) : Listener {

    @EventHandler
    fun onTntUse(event: PlayerInteractEvent) {
        if (arenaManager.getArena(event.player) == null) return
        val item = event.item ?: return
        if (item.type == Material.TNT) {
            event.isCancelled = true
            val player = event.player
            if (player.inventory.containsAtLeast(org.bukkit.inventory.ItemStack(Material.TNT), 1)) {
                player.inventory.removeItem(org.bukkit.inventory.ItemStack(Material.TNT, 1))
            }
            val dir = player.location.direction
            val spawn = player.eyeLocation.add(dir.clone().multiply(1.5))
            val tnt = player.world.spawn(spawn, TNTPrimed::class.java)
            tnt.fuseTicks = 40
            tnt.yield = 4f
            tnt.velocity = dir.clone().multiply(1.2)
        }
    }

    @EventHandler
    fun onWaterBucket(event: PlayerBucketEmptyEvent) {
        if (arenaManager.getArena(event.player) == null) return
        if (event.bucket == Material.WATER_BUCKET) {
            Bukkit.getScheduler().runTask(Disasters.getInstance(), Runnable {
                event.player.inventory.setItem(event.hand, org.bukkit.inventory.ItemStack(Material.BUCKET))
            })
        }
    }

    @EventHandler
    fun onMilkDrink(event: PlayerItemConsumeEvent) {
        if (arenaManager.getArena(event.player) == null) return
        if (event.item.type == Material.MILK_BUCKET) {
            Bukkit.getScheduler().runTask(Disasters.getInstance(), Runnable {
                val hand = event.hand
                if (hand != null) {
                    event.player.inventory.setItem(hand, org.bukkit.inventory.ItemStack(Material.BUCKET))
                }
            })
        }
    }
}
