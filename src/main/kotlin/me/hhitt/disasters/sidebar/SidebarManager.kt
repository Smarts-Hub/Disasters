package me.hhitt.disasters.sidebar

import fr.mrmicky.fastboard.adventure.FastBoard
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameState
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Msg
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID

/**
 * SidebarManager is responsible for managing the sidebars for players in the game.
 * It creates and updates the sidebars based on the game state.
 */

class SidebarManager {

    private val config = FileManager.get("scoreboard")!!
    private val mainConfig = FileManager.get("config")!!
    private val boards = mutableMapOf<UUID, FastBoard>()
    private val states = mutableMapOf<UUID, GameState?>()

    fun updateSidebar(player: Player, state: GameState?, arena: Arena? = null) {
        val playerId = player.uniqueId

        val board = boards.computeIfAbsent(playerId) {
            FastBoard(player)
        }

        val isNewPlayer = !states.containsKey(playerId)
        val stateChanged = states[playerId] != state
        val isDynamic = state == GameState.COUNTDOWN || state == GameState.LIVE

        if (isNewPlayer || stateChanged || isDynamic) {
            updateBoardContent(board, state, player, arena)
            states[playerId] = state
        }
    }

    private fun updateBoardContent(board: FastBoard, state: GameState?, player: Player, arena: Arena? = null) {
        when (state) {
            GameState.RECRUITING -> {
                board.updateTitle(Msg.parse(config.getString("recruiting.title")!!, player))
                val lines = config.getStringList("recruiting.lines")
                board.updateLines(parseLines(lines, player))
            }
            GameState.COUNTDOWN -> {
                board.updateTitle(Msg.parse(config.getString("countdown.title")!!, player))
                val lines = config.getStringList("countdown.lines")
                board.updateLines(parseLines(lines, player))
            }
            GameState.LIVE -> {
                board.updateTitle(Msg.parse(config.getString("live.title")!!, player))
                val lines = config.getStringList("live.lines")
                val parsed = parseLines(lines, player).toMutableList()

                // Append active disaster names if enabled in config
                if (arena != null && mainConfig.getBoolean("show-active-disasters", false)) {
                    val disasters = arena.disasters
                    if (disasters.isNotEmpty()) {
                        parsed.add(Component.empty())
                        val header = config.getString("live.disasters-header", "<gray>Disasters:")!!
                        parsed.add(Msg.parse(header, player))
                        val format = config.getString("live.disaster-format", "<green>  %disaster_name%")!!
                        disasters.forEach { disaster ->
                            val name = formatDisasterName(disaster::class.simpleName ?: "Unknown")
                            parsed.add(Msg.parse(format.replace("%disaster_name%", name), player))
                        }
                        val footer = config.getString("live.disasters-footer")
                        if (footer != null) {
                            parsed.add(Component.empty())
                            parsed.add(Msg.parse(footer, player))
                        }
                    }
                }

                board.updateLines(parsed)
            }
            GameState.RESTARTING -> {
                board.updateTitle(Msg.parse(config.getString("restarting.title")!!, player))
                val lines = config.getStringList("restarting.lines")
                board.updateLines(parseLines(lines, player))
            }
            else -> {
                board.updateTitle(Msg.parse(config.getString("lobby.title")!!, player))
                val lines = config.getStringList("lobby.lines")
                board.updateLines(parseLines(lines, player))
            }
        }
    }

    /**
     * Converts a class name like "MeteorShower" or "FloorIsLava" into a readable
     * display name like "Meteor Shower" or "Floor Is Lava".
     */
    private fun formatDisasterName(className: String): String {
        return className.replace(Regex("([a-z])([A-Z])"), "$1 $2")
    }

    private fun parseLines(lines: List<String>, player: Player): List<Component> {
        return lines.map { line -> Msg.parse(line, player) }
    }

    fun cleanupOfflinePlayers() {
        boards.entries.removeAll { (uuid, board) ->
            val player = org.bukkit.Bukkit.getPlayer(uuid)
            if (player == null || !player.isOnline) {
                board.delete()
                states.remove(uuid)
                true
            } else {
                false
            }
        }
    }

    fun removeBoard(player: Player) {
        val board = boards.remove(player.uniqueId)
        states.remove(player.uniqueId)
        board?.delete()
    }

    fun updateAllBoards() {
        boards.entries.removeAll { (uuid, board) ->
            val player = org.bukkit.Bukkit.getPlayer(uuid)
            if (player == null || !player.isOnline) {
                board.delete()
                states.remove(uuid)
                true
            } else {
                false
            }
        }
    }
}