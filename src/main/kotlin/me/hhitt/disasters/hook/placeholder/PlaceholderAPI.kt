package me.hhitt.disasters.hook.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.data.Data
import org.bukkit.entity.Player

class PlaceholderAPI(private val arenaManager: ArenaManager) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "disasters"
    }

    override fun getAuthor(): String {
        return "hhitt"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

//    override fun onPlaceholderRequest(player: Player, params: String): String {
//        return when (params) {
//            "wins" -> {
//                Data.getWins(player.uniqueId).toString()
//            }
//            "defeats" -> {
//                Data.getDefeats(player.uniqueId).toString()
//            }
//            "totalPlayed" -> {
//                Data.getTotalPlayed(player.uniqueId).toString()
//            }
//            "arena" -> {
//                arenaManager.getArena(player)?.displayName ?: "Not in an arena"
//            }
//            else -> {
//                "Invalid placeholder"
//            }
//        }
//    }
}