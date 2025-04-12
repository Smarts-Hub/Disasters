package me.hhitt.disasters.util

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.storage.file.FileManager
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.configuration.file.FileConfiguration

object Notify {

    /*
     * This class is used to send notifications to players in the arena.
     */

    private val config: FileConfiguration = FileManager.get("lang")!!

    fun countdown(arena: Arena, index: Int) {
        val path = "countdown.$index"
        val title = config.getString("$path.title") ?: ""
        val subtitle = config.getString("$path.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
        sound(arena, Sound.ENTITY_PLAYER_LEVELUP)
    }

    fun countdownCanceled(arena: Arena) {
        val title = config.getString("countdown-canceled.title") ?: ""
        val subtitle = config.getString("countdown-canceled.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BASS)
    }

    fun gameStart(arena: Arena) {
        val title = config.getString("game-start.title") ?: ""
        val subtitle = config.getString("game-start.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BELL)
    }

    fun gameEnd(arena: Arena) {
        val title = config.getString("game-end.title") ?: ""
        val subtitle = config.getString("game-end.subtitle") ?: ""
        sendTitleToArena(arena, title, subtitle)
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BASS)
    }

    fun disaster(arena: Arena, disaster: String) {
        val path = "disaster.$disaster"
        val title = config.getString("$path.title") ?: ""
        val subtitle = config.getString("$path.subtitle") ?: ""
        val chatMessages = config.getStringList("$path.chat")

        sendTitleToArena(arena, title, subtitle)
        sendChatMessagesToArena(arena, chatMessages)
        sound(arena, Sound.ENTITY_ENDER_DRAGON_SHOOT)
    }

    fun playerJoined(player: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-joined") ?: ""
        val msg = message.replace("%joined%", player.name)
        sendChatMessageToArena(arena, msg)
        sound(arena, Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
    }

    fun playerLeft(player: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-left") ?: ""
        val msg = message.replace("%left%", player.name)
        sendChatMessageToArena(arena, msg)
        sound(arena, Sound.ENTITY_ALLAY_DEATH)
    }

    fun playerKilled(player: Player, killer: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-killed") ?: ""
        val msg = message.replace("%killer%", player.name)
        sendChatMessageToArena(arena, msg)
    }

    fun playerDied(player: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-died") ?: ""
        val msg = message.replace("%dead%", player.name)
        sendChatMessageToArena(arena, msg)
    }

    fun playerWon(player: Player, arena: Arena) {
        val message = config.getString("game-broadcast.player-won") ?: ""
        val msg = message.replace("%winner%", player.name)
        sendChatMessageToArena(arena, msg)
        sound(arena, Sound.ENTITY_ENDER_DRAGON_DEATH)
    }

    private fun sendTitleToArena(arena: Arena, title: String, subtitle: String) {
        arena.playing.forEach { player ->
            Msg.sendTitle(player, title)
            Msg.sendSubtitle(player, subtitle)
        }
    }

    private fun sendChatMessagesToArena(arena: Arena, messages: List<String>) {
        arena.playing.forEach {
            player -> messages.forEach {
                message -> Msg.sendParsed(player, message)
            }
        }
    }

    private fun sendChatMessageToArena(arena: Arena, message: String) {
        arena.playing.forEach {
            player -> Msg.sendParsed(player, message)
        }
    }

    private fun sound(arena: Arena, sound: Sound) {
        arena.playing.forEach { player ->
            player.playSound(player.location, sound, 1.0f, 1.0f)
        }
    }


}
