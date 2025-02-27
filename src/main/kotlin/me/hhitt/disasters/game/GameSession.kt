package me.hhitt.disasters.game

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.countdown.Countdown
import me.hhitt.disasters.game.timer.GameTimer

class GameSession(private val arena: Arena) {

    private val countdown: Countdown = Countdown(arena)
    private val timer: GameTimer = GameTimer(arena)

    fun start() {
        when(arena.state) {
            GameState.RECRUITING -> countdown.start()
            GameState.COUNTDOWN -> timer.start()
            else -> {}
        }
    }

    fun stop() {
        when(arena.state) {
            GameState.COUNTDOWN -> countdown.cancel()
            GameState.LIVE -> timer.cancel()
            else -> {}
        }
    }

}