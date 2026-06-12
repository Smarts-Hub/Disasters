package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class Freeze : Disaster, TriggerTrackedDisaster, Listener {

    override var triggerCount: Int = 0
        private set

    private val arenas = mutableListOf<Arena>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Bukkit.getPluginManager().registerEvents(this, me.hhitt.disasters.Disasters.getInstance())
        Notify.disaster(arena, "freeze")
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val arena = arenas.firstOrNull { it.isPlayerValid(event.player) } ?: return
        if (event.from.blockX != event.to.blockX ||
            event.from.blockY != event.to.blockY ||
            event.from.blockZ != event.to.blockZ
        ) {
            event.to = event.from
            triggerCount++
        }
    }
}
