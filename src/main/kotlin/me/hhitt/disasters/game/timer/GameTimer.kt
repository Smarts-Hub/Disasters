package me.hhitt.disasters.game.timer

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.game.GameState
import org.bukkit.scheduler.BukkitRunnable

class GameTimer(private val arena: Arena): BukkitRunnable() {

    private var time = 0
    private var remaining = arena.countdown
    private val maxTime = arena.maxTime

    fun start() {
        this.run()
        arena.resetService.save()
        arena.playing.forEach { it.teleport(arena.location) }
        arena.state = GameState.LIVE
    }

    override fun cancel() {
        this.cancel()
        DisasterRegistry.removeDisasters(arena)
        time = 0
        remaining = arena.countdown
        arena.state = GameState.RESTARTING
        arena.resetService.paste()
    }

    override fun run() {

        if(time >= maxTime) {
            cancel()
            return
        }

        if(arena.alive.size <= arena.aliveToEnd) {
            cancel()
            return
        }

        if(time % arena.rate == 0) {
            DisasterRegistry.addRandomDisaster(arena)
        }

        time++
        remaining--
    }
}