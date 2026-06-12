package me.hhitt.disasters.game.countdown

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.game.modification.GameModificationRegistry
import me.hhitt.disasters.game.modification.vote.GameModificationVoteManager
import me.hhitt.disasters.util.Notify
import org.bukkit.scheduler.BukkitRunnable

class Countdown(private val arena: Arena, private val session: GameSession) : BukkitRunnable() {

    var time = 0
    var remaining = arena.countdown
    private var voteManager: GameModificationVoteManager? = null
    private var startingGame = false

    override fun run() {
        if (time >= arena.countdown) {
            if (time >= (arena.countdown + 2)) {
                startingGame = true
                Notify.gameStart(arena)

                val selectedModifications = voteManager?.resolveVote().orEmpty()
                GameModificationRegistry.start(arena, selectedModifications)

                cancel()
                session.startGameTimer()
                return
            }
            time++
            return
        }

        if (arena.alive.size <= arena.aliveToEnd) {
            cancel()
            return
        }

        if (time == 0 && voteManager == null) {
            val config = Disasters.getInstance().config
            if (config.getBoolean("game-modifications.voting.enabled", true)) {
                val manager = GameModificationVoteManager(arena)
                manager.startVote()
                voteManager = manager
            }
        }

        Notify.countdown(arena, remaining)
        time++
        remaining--
    }

    override fun cancel() {
        super.cancel()
        val wasStarting = startingGame
        voteManager = null
        if (!wasStarting) {
            Notify.countdownCanceled(arena)
        }
        time = 0
        remaining = arena.countdown
    }
}
