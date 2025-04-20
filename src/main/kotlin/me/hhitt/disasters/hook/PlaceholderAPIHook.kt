package me.hhitt.disasters.hook

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.data.Data
import org.bukkit.entity.Player

/**
 * PlaceholderAPI Hook for Disasters plugin.
 *
 * @param arenaManager The ArenaManager instance to manage arenas.
 */

class PlaceholderAPIHook(private val arenaManager: ArenaManager) : PlaceholderExpansion() {

    val plugin = Disasters.getInstance()

    override fun getIdentifier(): String {
        return "disasters"
    }

    override fun getAuthor(): String {
        return "hhitt"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onPlaceholderRequest(player: Player, params: String): String {
         return when(params) {
            "wins" -> {
                Data.getWinsFromCache(player.uniqueId).toString()
            }
            "defeats" -> {
                Data.getDefeatsFromCache(player.uniqueId).toString()
            }
            "total_played" -> {
                Data.getTotalPlayedFromCache(player.uniqueId).toString()
            }
            "arena" -> {
                arenaManager.getArena(player)?.displayName ?: "Not in an arena"
            }
            "is_in_arena" -> {
                arenaManager.getArena(player)?.displayName?.let { "true" } ?: "false"
            }
            "game_players" -> {
                arenaManager.getArena(player)?.playing?.size.toString()
            }
            "game_max_players" -> {
                arenaManager.getArena(player)?.maxPlayers.toString()
            }
            "game_min_players" -> {
                arenaManager.getArena(player)?.minPlayers.toString()
            }
            "game_state" -> {
                arenaManager.getArena(player)?.state.toString()
            }
            "game_time" -> {
                arenaManager.getArena(player)?.getGameTime().toString()
            }
            "game_time_left" -> {
                arenaManager.getArena(player)?.getTimeLeft().toString()
            }
            "countdown_time" -> {
                arenaManager.getArena(player)?.getCountdownTime().toString()
            }
            "countdown_time_left" -> {
                arenaManager.getArena(player)?.getCountdownLeft().toString()
            }
            else -> {
                "Invalid placeholder"
            }
        }
    }
}