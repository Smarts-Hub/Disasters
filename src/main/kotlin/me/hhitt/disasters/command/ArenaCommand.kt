package me.hhitt.disasters.command

import me.hhitt.disasters.arena.ArenaManager
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.actor.BukkitCommandActor

@Command("arena")
class ArenaCommand(private val arenaManager: ArenaManager) {

    @Subcommand("join <arena>")
    fun join(actor: BukkitCommandActor, arena: String) {
        //Checking if the actor is a player and obtaining it
        if(!actor.isPlayer) return
        val player = actor.asPlayer()!!

        // Getting the arena from the ArenaManager
        arenaManager.getArena(arena)?.let {
            // Checking if the arena is full
            if(it.isFull()){
                player.sendMessage("§cThat arena is full.")
                return
            }
            // Checking if it is recruiting or counting down
            if(!it.isWaiting()){
                player.sendMessage("§cThat arena is not waiting for players.")
                return
            }
            it.addPlayer(player)
        } ?: run {
            // If the arena does not exist
            player.sendMessage("§cThat arena does not exist.")
        }
    }

    @Subcommand("leave")
    fun leave(actor: BukkitCommandActor) {
        //Checking if the actor is a player and obtaining it
        if(!actor.isPlayer) return
        val player = actor.asPlayer()!!

        // Getting the arena from the ArenaManager
        arenaManager.getArena(player)?.let {
            it.removePlayer(player)
        } ?: run {
            player.sendMessage("§cYou are not in an arena.")
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
                player.sendMessage("§cYou do not have permission to do that.")
                return
            }
            it.start()
        } ?: run {
            // If the arena does not exist
            player.sendMessage("§cYou are not in an arena.")
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
                player.sendMessage("§cYou do not have permission to do that.")
                return
            }
            it.stop()
        } ?: run {
            // If the arena does not exist
            player.sendMessage("§cYou are not in an arena.")
        }
    }

    @Subcommand("forcestart <arena>")
    fun forceStart(actor: BukkitCommandActor, arena: String) {
        val sender = actor.sender()

        // Getting the arena from the ArenaManager
        arenaManager.getArena(arena)?.let {
            // Checking if the player has permission
            if(!sender.hasPermission("disasters.forcestart")){
                sender.sendMessage("§cYou do not have permission to do that.")
                return
            }
            it.start()
        } ?: run {
            // If the arena does not exist
            sender.sendMessage("§cThat arena does not exist.")
        }
    }

    @Subcommand("forcestop <arena>")
    fun forceStop(actor: BukkitCommandActor, arena: String) {
        val sender = actor.sender()

        // Getting the arena from the ArenaManager
        arenaManager.getArena(arena)?.let {
            // Checking if the player has permission
            if(!sender.hasPermission("disasters.forcestop")){
                sender.sendMessage("§cYou do not have permission to do that.")
                return
            }
            it.stop()
        } ?: run {
            // If the arena does not exist
            sender.sendMessage("§cThat arena does not exist.")
        }
    }
}