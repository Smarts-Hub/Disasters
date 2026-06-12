package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * MirrorControls reverses horizontal displacement server-side.
 * Bukkit/Paper exposes movement/input reads but no raw client key rewrite setter,
 * so this disaster applies an inverted target via PlayerMoveEvent to simulate reversed controls.
 */
class MirrorControls : Disaster, Listener {

    private val arenas = mutableListOf<Arena>()
    private val guard = ConcurrentHashMap<UUID, Long>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        Bukkit.getPluginManager().registerEvents(this, me.hhitt.disasters.Disasters.getInstance())
        Notify.disaster(arena, "mirror-controls")
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
        if (event.from.block == event.to.block) return
        val now = System.currentTimeMillis()
        val last = guard[event.player.uniqueId] ?: 0L
        if (now - last < 50) return
        val dx = event.to.x - event.from.x
        val dz = event.to.z - event.from.z
        if (dx == 0.0 && dz == 0.0) return
        val inverted = event.from.clone()
        inverted.x = event.from.x - dx
        inverted.z = event.from.z - dz
        inverted.yaw = event.to.yaw
        inverted.pitch = event.to.pitch
        inverted.y = event.to.y
        event.to = inverted
        guard[event.player.uniqueId] = now
    }
}
