package me.hhitt.disasters.util

import me.clip.placeholderapi.PlaceholderAPI
import me.hhitt.disasters.storage.file.FileManager
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.TitlePart
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Msg {

    /*
     * This class is used to parse messages from the config file.
     * It uses MiniMessage to parse the messages and PlaceholderAPI to set placeholders.
     * It also has methods to send messages, titles, subtitles, action bars and sounds to players.
     * Some of the methods are not used yet, but in a near future they will be used.
     */

    private val miniMsg = MiniMessage.miniMessage()

    fun parse(msg : String, player: Player) : TextComponent {
        return miniMsg.deserialize(placeholder(msg, player)) as TextComponent
    }

    fun parse(msg : String) : TextComponent {
        return miniMsg.deserialize(msg) as TextComponent
    }

    fun parseList(lore: List<String>, player: Player) : List<TextComponent> {
        val components = mutableListOf<TextComponent>()
        lore.forEach {
            components.add(parse(it, player))
        }
        return components
    }

    fun send(player: Player, path: String){
        player.sendMessage(parse(getMsg(path), player))
    }

    fun send(sender: CommandSender, path: String){
        sender.sendMessage(parse(getMsg(path)))
    }

    fun sendParsed(player: Player, msg: String){
        player.sendMessage(parse(msg, player))
    }


    fun sendTitle(player: Player, title: String){
        player.sendTitlePart(TitlePart.TITLE, parse(title, player))
    }

    fun sendSubtitle(player: Player, subtitle: String){
        player.sendTitlePart(TitlePart.SUBTITLE, parse(subtitle, player))
    }

    fun sendActionbar(player: Player, bar: String){
        player.sendActionBar(parse(bar, player))
    }

    fun playSound(player: Player, sound: String){
        player.playSound(player.location, sound, 1f, 1f)
    }

    private fun getMsg(path: String) : String {
        return FileManager.get("lang")?.getString("messages.$path") ?: "Message not found"
    }

    private fun placeholder(msg: String, player: Player) : String {
        return PlaceholderAPI.setPlaceholders(player, msg)
    }

}