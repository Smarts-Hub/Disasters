package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class SimonSays : Disaster, TriggerTrackedDisaster, Listener {

    override var triggerCount: Int = 0
        private set

    private enum class Action { JUMP, SNEAK, CLICK }

    private val arenas = mutableListOf<Arena>()
    private var currentAction: Action = Action.JUMP
    private var currentDeadlineTick: Int = -1
    private var nextRoundTick: Int = 0
    private val completed = ConcurrentHashMap<UUID, Boolean>()

    override fun start(arena: Arena) {
        currentDeadlineTick = -1
        nextRoundTick = 0
        completed.clear()
        arenas.add(arena)
        Bukkit.getPluginManager().registerEvents(this, me.hhitt.disasters.Disasters.getInstance())
        Notify.disaster(arena, "simon-says")
    }

    override fun pulse(time: Int) {
        if (currentDeadlineTick == -1) {
            startRound(time)
            return
        }
        if (time >= currentDeadlineTick) {
            if (arenas.isNotEmpty()) punish(arenas[0])
            startRound(time)
        }
    }

    private fun startRound(time: Int) {
        if (arenas.isEmpty()) return
        currentAction = Action.values().random()
        completed.clear()
        val title = when (currentAction) {
            Action.JUMP -> "<green><bold>JUMP"
            Action.SNEAK -> "<green><bold>SNEAK"
            Action.CLICK -> "<green><bold>CLICK"
        }
        arenas[0].alive.toList().forEach { p -> Notify.playerMessageRaw(p, title) }
        currentDeadlineTick = time + 4
        nextRoundTick = time + 6
        triggerCount++
    }

    private fun punish(arena: Arena) {
        arena.alive.toList().forEach { player ->
            if (completed[player.uniqueId] != true) {
                DeathMessageService.mark(player, "simon-says")
                player.world.strikeLightningEffect(player.location)
                player.damage(6.0)
            }
        }
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        completed.clear()
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (currentAction != Action.JUMP) return
        if (event.from.y < event.to.y && (event.to.y - event.from.y) > 0.1) {
            completed[event.player.uniqueId] = true
        }
    }

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        if (currentAction != Action.SNEAK) return
        if (event.isSneaking) {
            completed[event.player.uniqueId] = true
        }
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        if (currentAction != Action.CLICK) return
        completed[event.player.uniqueId] = true
    }
}
