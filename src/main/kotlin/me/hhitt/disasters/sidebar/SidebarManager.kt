package me.hhitt.disasters.sidebar

import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Msg
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import org.bukkit.entity.Player

/**
 * SidebarManager is responsible for managing the sidebars for players in the game.
 * It creates and updates the sidebars based on the game state.
 *
 * @param scoreboardLibrary The ScoreboardLibrary instance used to create sidebars.
 */

class SidebarManager(private val scoreboardLibrary: ScoreboardLibrary) {

    private val config = FileManager.get("scoreboard")!!
    private val sidebars = mutableMapOf<Player, Sidebar>()
    private val states = mutableMapOf<Player, GameState?>()

    fun updateSidebar(player: Player, state: GameState?) {
        sidebars[player]?.removePlayer(player)
        val sidebar = sidebarBuilder(state, player)
        sidebar.addPlayer(player)
        sidebars[player] = sidebar
    }

    private fun sidebarBuilder(state: GameState?, player: Player): Sidebar {

        if(state == states[player]) {
            return sidebars[player]!!
        }

        val sidebar = scoreboardLibrary.createSidebar()

        when (state) {
            GameState.RECRUITING -> {
                sidebar.title(Msg.parse(config.getString("recruiting.title")!!, player))
                val lines = config.getStringList("recruiting.lines")
                setSidebarLines(sidebar, lines, player)
            }
            GameState.COUNTDOWN -> {
                sidebar.title(Msg.parse(config.getString("countdown.title")!!, player))
                val lines = config.getStringList("countdown.lines")
                setSidebarLines(sidebar, lines, player)
            }
            GameState.LIVE -> {
                sidebar.title(Msg.parse(config.getString("live.title")!!, player))
                val lines = config.getStringList("live.lines")
                setSidebarLines(sidebar, lines, player)
            }
            GameState.RESTARTING -> {
                sidebar.title(Msg.parse(config.getString("restarting.title")!!, player))
                val lines = config.getStringList("restarting.lines")
                setSidebarLines(sidebar, lines, player)
            }
            else -> {
                sidebar.title(Msg.parse(config.getString("lobby.title")!!, player))
                val lines = config.getStringList("lobby.lines")
                setSidebarLines(sidebar, lines, player)
            }
        }

        states[player] = state
        return sidebar
    }

    private fun setSidebarLines(sidebar: Sidebar, lines: List<String>, player: Player) {
        lines.forEachIndexed { index, line ->
            val parsedLine = Msg.parse(line, player)
            sidebar.line(index, parsedLine)
        }
    }

}
