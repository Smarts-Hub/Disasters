package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class Disco : Disaster, Listener {

    private val arenas = mutableListOf<Arena>()
    private val lastMove = ConcurrentHashMap<UUID, Long>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        arena.alive.forEach { lastMove[it.uniqueId] = System.currentTimeMillis() }
        Bukkit.getPluginManager().registerEvents(this, me.hhitt.disasters.Disasters.getInstance())
        Notify.disaster(arena, "disco")
    }

    override fun pulse(time: Int) {
        if (time % 2 != 0) return
        val now = System.currentTimeMillis()
        val colors = listOf(
            Color.RED, Color.AQUA, Color.YELLOW, Color.LIME, Color.FUCHSIA, Color.ORANGE, Color.PURPLE
        )
        arenas.toList().forEach { arena ->
            arena.alive.toList().forEach { player ->
                val c = colors.random()
                player.world.spawnParticle(Particle.DUST, player.location.add(0.0, 1.0, 0.0), 6, 0.3, 0.3, 0.3, Particle.DustOptions(c, 1.2f))
                if (time % 4 == 0) {
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_HARP, 0.6f, 1.4f)
                }
                val last = lastMove[player.uniqueId] ?: now
                if (now - last > 1000) {
                    DeathMessageService.mark(player, "disco")
                    player.damage(2.0)
                    lastMove[player.uniqueId] = now
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.from.blockX == event.to.blockX && event.from.blockZ == event.to.blockZ) return
        lastMove[event.player.uniqueId] = System.currentTimeMillis()
    }
}
