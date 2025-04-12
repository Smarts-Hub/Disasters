package me.hhitt.disasters.command

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Msg
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.actor.BukkitCommandActor

/**
 * ArenaCommand class that handles the commands related to arena prefix.
 *
 * @param arenaManager The ArenaManager instance used to manage arenas.
 */

@Command("arena")
class ArenaCommand(private val arenaManager: ArenaManager) {

    private val lang = FileManager.get("lang")!!

    @Subcommand("join <arena>")
    fun join(actor: BukkitCommandActor, arena: String) {
        //Checking if the actor is a player and obtaining it
        if(!actor.isPlayer) return
        val player = actor.asPlayer()!!

        arenaManager.getArena(player)?.let {
            Msg.send(player, "already-in-arena")
            return
        }

        // Getting the arena from the ArenaManager
        arenaManager.getArena(arena)?.let {
            // Checking if the arena is full
            if(it.isFull()){
                Msg.send(player, "arena-full")
                return
            }
            // Checking if it is recruiting or counting down
            if(!it.isWaiting()){
                Msg.send(player, "arena-in-game")
                return
            }
            it.addPlayer(player)
        } ?: run {
            // If the arena does not exist
            Msg.send(player, "arena-not-found")
        }
    }

    @Subcommand("quickjoin")
    fun quickJoin(actor: BukkitCommandActor) {
        if(!actor.isPlayer) return
        val player = actor.asPlayer()!!

        arenaManager.getArena(player)?.let {
            Msg.send(player, "already-in-arena")
            return
        }

        arenaManager.addPlayerToBestArena(player)
    }

    @Subcommand("leave")
    fun leave(actor: BukkitCommandActor) {
        //Checking if the actor is a player and obtaining it
        if(!actor.isPlayer) return
        val player = actor.asPlayer()!!

        // Getting the arena from the ArenaManager
        arenaManager.getArena(player)?.removePlayer(player) ?: run {
            Msg.send(player, "not-in-arena")
        }
    }

    @Subcommand("forcestart")
    fun forceStart(actor: BukkitCommandActor) {
        //Checking if the actor is a player and obtaining it
        if(!actor.isPlayer) return
        val player = actor.asPlayer()!!

        // Getting the arena from the ArenaManager
        arenaManager.getArena(player)?.let {
            // Checking if the player has permission
            if(!player.hasPermission("disasters.forcestart")){
                Msg.send(player, "no-permission")

                return
            }
            it.start()
        } ?: run {
            // If the arena does not exist
            Msg.send(player, "not-in-arena")
        }
    }

    @Subcommand("forcestop")
    fun forceStop(actor: BukkitCommandActor) {
        //Checking if the actor is a player and obtaining it
        if(!actor.isPlayer) return
        val player = actor.asPlayer()!!

        // Getting the arena from the ArenaManager
        arenaManager.getArena(player)?.let {
            // Checking if the player has permission
            if(!player.hasPermission("disasters.forcestop")){
                Msg.send(player, "no-permission")
                return
            }
            it.stop()
        } ?: run {
            // If the arena does not exist
            Msg.send(player, "not-in-arena")
        }
    }

    @Subcommand("forcestart <arena>")
    fun forceStart(actor: BukkitCommandActor, arena: String) {
        val sender = actor.sender()

        // Getting the arena from the ArenaManager
        arenaManager.getArena(arena)?.let {
            // Checking if the player has permission
            if(!sender.hasPermission("disasters.forcestart")){
                Msg.send(sender, "no-permission")
                return
            }
            it.start()
        } ?: run {
            // If the arena does not exist
            Msg.send(sender, "arena-not-found")
        }
    }

    @Subcommand("forcestop <arena>")
    fun forceStop(actor: BukkitCommandActor, arena: String) {
        val sender = actor.sender()

        // Getting the arena from the ArenaManager
        arenaManager.getArena(arena)?.let {
            // Checking if the player has permission
            if(!sender.hasPermission("disasters.forcestop")){
                Msg.send(sender, "no-permission")
                return
            }
            it.stop()
        } ?: run {
            // If the arena does not exist
            Msg.send(sender, "arena-not-found")
        }
    }
}