package me.hhitt.disasters.game.countdown

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.game.vote.VoteManager
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
    private var voteManager: VoteManager? = null

    override fun run() {
        if (time >= arena.countdown) {
            if(time >= (arena.countdown + 2)) {
                Notify.gameStart(arena)

                // Resolve vote BEFORE cancel() which clears voteManager
                voteManager?.let { manager ->
                    arena.gameMode = manager.resolveVote()
                }

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

        // Open vote GUI when 5 seconds remain in countdown
        if (time == 0 && voteManager == null) {
            val config = Disasters.getInstance().config
            if (config.getBoolean("voting.enabled", true)) {
                voteManager = VoteManager(arena)
                voteManager?.startVote()
            }
        }

        Notify.countdown(arena, remaining)
        time++
        remaining--
    }

    override fun cancel() {
        super.cancel()
        voteManager = null
        Notify.countdownCanceled(arena)
        time = 0
        remaining = arena.countdown
    }
}