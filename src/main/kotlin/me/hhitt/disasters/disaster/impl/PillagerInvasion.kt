package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import me.hhitt.disasters.util.SpawnLocationFinder
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Pillager
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class PillagerInvasion : Disaster, TriggerTrackedDisaster {

    override var triggerCount: Int = 0
        private set

    private val arenas = mutableListOf<Arena>()
    private val pillagers = mutableMapOf<Arena, MutableList<Pillager>>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        pillagers[arena] = mutableListOf()
        Notify.disaster(arena, "pillager-invasion")
    }

    override fun pulse(time: Int) {
        if (time % 10 != 0) return
        arenas.toList().forEach { arena ->
            val list = pillagers[arena] ?: return@forEach
            list.removeAll { it.isDead }
            if (list.size > 30) return@forEach
            arena.alive.toList().forEach { player ->
                val spawnLoc = SpawnLocationFinder.findNearPlayer(arena, player.location, 8, 14, 3) ?: return@forEach
                val target = arena.alive.filter { it != player }.minByOrNull { it.location.distanceSquared(spawnLoc) } ?: player
                val pillager = spawnLoc.world.spawn(spawnLoc, Pillager::class.java)
                pillager.inventory.setItem(0, ItemStack(Material.CROSSBOW))
                pillager.target = target
                list.add(pillager)
                triggerCount++
            }
        }
    }

    override fun stop(arena: Arena) {
        pillagers.remove(arena)?.forEach { if (!it.isDead) it.remove() }
        arenas.remove(arena)
    }
}
