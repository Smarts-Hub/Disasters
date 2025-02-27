package me.hhitt.disasters.util

import me.clip.placeholderapi.PlaceholderAPI
import me.hhitt.disasters.storage.file.FileManager
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.TitlePart
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Msg {

    private val miniMsg = MiniMessage.miniMessage()

    fun parse(msg : String) : TextComponent {
        return miniMsg.deserialize(msg) as TextComponent
    }

    fun parseList(lore: List<String>) : List<TextComponent> {
        val components = mutableListOf<TextComponent>()
        lore.forEach {
            components.add(parse(it))
        }
        return components
    }

    fun sendMsg(path: String, player: Player) {
        val msg = placeholder(getMsg(path), player)
        player.sendMessage(parse(msg))
    }

    fun sendMsg(path: String, sender: CommandSender) {
        sender.sendMessage(parse(getMsg(path)))
    }

    fun sendLiteralMsg(msg: String, player: Player) {
        player.sendMessage(parse(msg))
    }

    fun sendTitle(player: Player, title: String){
        player.sendTitlePart(TitlePart.TITLE, parse(title))
    }

    fun sendSubtitle(player: Player, subtitle: String){
        player.sendTitlePart(TitlePart.SUBTITLE, parse(subtitle))
    }

    fun sendActionbar(player: Player, bar: String){
        player.sendActionBar(parse(bar))
    }

    fun playSound(player: Player, sound: String){
        player.playSound(player.location, sound, 1f, 1f)
    }

    // Obtain message from lang.yml
    private fun getMsg(path: String) : String {
        return FileManager.get("messages")?.getString(path) ?: "Message not found"
    }

    // Apply placeholders to message
    private fun placeholder(msg: String, player: Player) : String {
        return PlaceholderAPI.setPlaceholders(player, msg)
    }

}