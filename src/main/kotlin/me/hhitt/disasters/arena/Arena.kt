package me.hhitt.disasters.arena

import me.hhitt.disasters.arena.services.Border
import me.hhitt.disasters.arena.services.ResetArenaService
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.game.GameSession
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.util.Lobby
import org.bukkit.Location
import org.bukkit.entity.Player

class Arena(
    val name: String,
    val displayName: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val aliveToEnd: Int,
    val maxTime: Int,
    val countdown: Int,
    val rate: Int,
    val maxDisasters: Int,
    val location: Location,
    val corner1: Location,
    val corner2: Location
) {

    val playing: MutableList<Player> = mutableListOf()
    val alive: MutableList<Player> = mutableListOf()
    val disasters: MutableList<Disaster> = mutableListOf()
    var state = GameState.RECRUITING
    val border = Border(corner1, corner2)
    val resetService = ResetArenaService(corner1, corner2)
    private val gameSession = GameSession(this)

    fun addPlayer(player: Player) {
        playing.add(player)
        alive.add(player)
        player.teleport(location)
        if(playing.size == minPlayers) {
            start()
        }
    }

    fun removePlayer(player: Player) {
        Lobby.teleportPlayer(player)
        playing.remove(player)
        alive.remove(player)
    }

    fun isFull(): Boolean {
        return (playing.size == maxPlayers)
    }

    fun isEmpty(): Boolean {
        return playing.size == 0
    }

    fun isWaiting(): Boolean {
        return state == GameState.RECRUITING || state == GameState.COUNTDOWN
    }

    fun isPlayerValid(player: Player): Boolean {
        return playing.contains(player)
    }

    fun start() {
        gameSession.start()
    }

    fun stop() {
        gameSession.stop()
    }

}