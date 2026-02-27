package me.hhitt.disasters.game.timer

import com.github.shynixn.mccoroutine.bukkit.launch
import me.clip.placeholderapi.PlaceholderAPI
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import me.hhitt.disasters.disaster.impl.BlockDisappear
import me.hhitt.disasters.disaster.impl.FloorIsLava
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Notify
import org.bukkit.Bukkit
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
    val remaining: Int get() = arena.maxTime - time

    override fun run() {
        if (time >= arena.maxTime) {
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

        // Fix: Use type check (any { it is T }) instead of contains(T()) which fails on reference equality
        if (arena.disasters.any { it is FloorIsLava }) {
            arena.alive.forEach { player ->
                DisasterRegistry.addBlockToFloorIsLava(arena, player.location)
            }
        }

        if (arena.disasters.any { it is BlockDisappear }) {
            arena.alive.forEach { player ->
                DisasterRegistry.addBlockToDisappear(arena, player.location)
            }
        }

        time++
    }

    override fun cancel() {
        // Async
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

        // Main thread
        arena.playing.forEach { player ->
            // Loser commands
            if (!arena.alive.contains(player)) {
                for(command in arena.losersCommands) {
                    val commandParsed = PlaceholderAPI.setPlaceholders(player, command)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandParsed)
                }
            }
            // Winner commands
            if(arena.alive.contains(player)) {
                for(command in arena.winnersCommands) {
                    val commandParsed = PlaceholderAPI.setPlaceholders(player, command)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandParsed)
                }
            }
            // To-all commands
            for(command in arena.toAllCommands) {
                val commandParsed = PlaceholderAPI.setPlaceholders(player, command)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandParsed)
            }
        }

        Lobby.teleportAtEnd(arena)
        arena.state = GameState.RESTARTING
        super.cancel()
        Notify.gameEnd(arena)
        DisasterRegistry.removeDisasters(arena)
        time = 0
        arena.state = GameState.RECRUITING
        arena.resetService.paste()
    }
}