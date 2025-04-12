package me.hhitt.disasters.game.countdown

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.util.Notify
import org.bukkit.scheduler.BukkitRunnable

/**
 * The Countdown class is responsible for managing the countdown timer before the game starts.
 * It handles the countdown time, remaining time, and notifies players about the countdown status.
 *
 * @param arena The arena where the countdown is taking place.
 * @param session The game session associated with the arena.
 */

class Countdown(private val arena: Arena, private val session: GameSession) : BukkitRunnable() {

    var time = 0
    var remaining = arena.countdown

    override fun run() {
        if (time >= arena.countdown) {
            if(time >= (arena.countdown + 2)) {
                Notify.gameStart(arena)
                cancel()
                session.startGameTimer()
            }
            time++
            return
        }

        if (arena.alive.size <= arena.aliveToEnd) {
            cancel()
            return
        }

        Notify.countdown(arena, remaining)
        time++
        remaining--
    }

    override fun cancel() {
        super.cancel()
        Notify.countdownCanceled(arena)
        time = 0
        remaining = arena.countdown
    }
}
