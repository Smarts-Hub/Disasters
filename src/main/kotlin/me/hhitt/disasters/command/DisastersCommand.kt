package me.hhitt.disasters.command

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.sidebar.SidebarService
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Lobby
import me.hhitt.disasters.util.Msg
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission

/**
 * DisastersCommand class that handles the commands related to disasters prefix.
 *
 * @param arenaManager The ArenaManager instance used to manage arenas.
 * @param sidebarService The SidebarService instance used to manage sidebars.
 */

@Command("disasters")
@CommandPermission("disasters.admin")
class DisastersCommand(private val arenaManager: ArenaManager, private val sidebarService: SidebarService) {

    @Subcommand("reload")
    fun reload(actor: BukkitCommandActor) {
        FileManager.get("config")!!.reloadFile()
        FileManager.get("lang")!!.reloadFile()
        arenaManager.reloadArenas()
        sidebarService.updateSidebar()
        Msg.send(actor.sender(), "reload-success")
    }


    @Subcommand("setspawn")
    fun setSpawn(actor: BukkitCommandActor) {
        if(!actor.isPlayer) return
        val player = actor.asPlayer()!!
        FileManager.get("config")!!.set("lobby.world", player.world.name)
        FileManager.get("config")!!.set("lobby.x", player.location.x)
        FileManager.get("config")!!.set("lobby.y", player.location.y)
        FileManager.get("config")!!.set("lobby.z", player.location.z)
        FileManager.get("config")!!.set("lobby.yaw", player.location.yaw)
        FileManager.get("config")!!.set("lobby.pitch", player.location.pitch)
        FileManager.get("config")!!.save()
        FileManager.reload("config")
        Lobby.setLocation()
        Msg.send(actor.sender(), "lobby-set")
    }
}