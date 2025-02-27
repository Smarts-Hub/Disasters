package me.hhitt.disasters

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.command.TestCommand
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Filer
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor

class Disasters : JavaPlugin() {

    companion object {
        private lateinit var instance: Disasters

        fun getInstance(): Disasters = instance
    }

    private lateinit var arenaManager: ArenaManager

    override fun onEnable() {

        instance = this
        Filer.createFolders()
        FileManager.initialize()
        //Lobby.setLocation()

        val lamp: Lamp<BukkitCommandActor> = BukkitLamp.builder(this)
            .build()
        lamp.register(TestCommand())

        arenaManager = ArenaManager()

    }

    override fun onDisable() {
    }

}
