package me.hhitt.disasters.game.modification.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.modification.GameModification
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Notify

class SwapperModification : GameModification {

    override val id: String = "swapper"
    override val displayName: String = "Swapper"

    private var elapsed: Int = 0
    private var lastSwapTick: Int = -1
    private var interval: Int = 10

    override fun start(arena: Arena) {
        elapsed = 0
        lastSwapTick = -1
        interval = FileManager.get("config")?.getInt("game-modifications.swapper.interval-seconds", 10) ?: 10
        Notify.disaster(arena, "swapper")
    }

    override fun pulse(arena: Arena, time: Int) {
        if (interval <= 0) return
        if (time - lastSwapTick >= interval) {
            val players = arena.alive.toMutableList()
            players.shuffle()
            for (i in 0 until players.size - 1 step 2) {
                val a = players[i]
                val b = players[i + 1]
                val locA = a.location
                val locB = b.location
                a.teleport(locB)
                b.teleport(locA)
            }
            lastSwapTick = time
        }
    }

    override fun stop(arena: Arena) {
        // no persistent state
    }
}
