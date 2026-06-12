package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Msg
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class RedLightGreenLight : Disaster, Listener {

    private val arenas = mutableListOf<Arena>()
    private var phaseRed = false
    private var lastPhaseChange = 0

    override fun start(arena: Arena) {
        arenas.add(arena)
        phaseRed = false
        lastPhaseChange = 0
        Bukkit.getPluginManager().registerEvents(this, me.hhitt.disasters.Disasters.getInstance())
        Notify.disaster(arena, "red-light-green-light")
    }

    override fun pulse(time: Int) {
        if (time - lastPhaseChange >= 4) {
            phaseRed = !phaseRed
            lastPhaseChange = time
            val title = if (phaseRed) "<red><bold>RED LIGHT" else "<green><bold>GREEN LIGHT"
            arenas.toList().forEach { arena ->
                arena.alive.forEach { p -> Msg.sendTitle(p, title) }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!phaseRed) return
        val arena = arenas.firstOrNull { it.isPlayerValid(event.player) } ?: return
        val dx = event.to.x - event.from.x
        val dy = event.to.y - event.from.y
        val dz = event.to.z - event.from.z
        if (dx * dx + dy * dy + dz * dz > 0.0009) {
            event.isCancelled = true
            DeathMessageService.mark(event.player, "red-light-green-light")
            event.player.world.strikeLightningEffect(event.to)
            event.player.damage(20.0)
        }
    }
}
