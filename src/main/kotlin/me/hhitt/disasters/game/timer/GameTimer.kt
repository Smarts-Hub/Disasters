package me.hhitt.disasters.game.timer

import com.github.shynixn.mccoroutine.bukkit.launch
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.disaster.impl.FloorIsLava
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Notify
import org.bukkit.scheduler.BukkitRunnable

/**
 * The GameTimer class is responsible for managing the game timer in an arena.
 * It handles the game time, remaining time, and disaster events during the game.
 *
 * @param arena The arena where the game is taking place.
 * @param session The game session associated with the arena.
 */

class GameTimer(private val arena: Arena, private val session: GameSession) : BukkitRunnable() {

    private val plugin = Disasters.getInstance()
    var time = 0
    private var remaining = arena.maxTime

    override fun run() {
        if (time >= remaining) {
            cancel()
            session.stop()
            return
        }

        if (arena.alive.size <= arena.aliveToEnd) {
            cancel()
            session.stop()
            return
        }

        if (time % arena.rate == 0) {
            DisasterRegistry.addRandomDisaster(arena)
        }

        if(arena.disasters.contains(FloorIsLava())){
            arena.alive.forEach { player ->
                DisasterRegistry.addBlockToFloorIsLava(arena, player.location)
            }
        }

        time++
        remaining--
    }

    override fun cancel() {
        plugin.launch {
            arena.playing.forEach { player ->
                Data.increaseTotalPlayed(player.uniqueId)
                if (!arena.alive.contains(player)) {
                    Data.increaseDefeats(player.uniqueId)
                }
                if(arena.alive.contains(player)) {
                    Data.increaseWins(player.uniqueId)
                }
            }

        }
        Lobby.teleportAtEnd(arena)
        arena.state = GameState.RESTARTING
        super.cancel()
        Notify.gameEnd(arena)
        DisasterRegistry.removeDisasters(arena)
        time = 0
        remaining = arena.maxTime
        arena.state = GameState.RECRUITING
        arena.resetService.paste()
    }
}
