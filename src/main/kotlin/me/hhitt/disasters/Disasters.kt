package me.hhitt.disasters

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.command.ArenaCommand
import me.hhitt.disasters.command.DisastersCommand
import me.hhitt.disasters.disaster.DisasterTask
import me.hhitt.disasters.hook.PlaceholderAPIHook
import me.hhitt.disasters.listener.*
import me.hhitt.disasters.sidebar.SidebarService
import me.hhitt.disasters.storage.data.Data
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Filer
import me.hhitt.disasters.util.Lobby
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.zapper.ZapperJavaPlugin


class Disasters : ZapperJavaPlugin() {

    // Singleton instance
    companion object {
        private lateinit var instance: Disasters
        fun getInstance(): Disasters = instance
    }

    private lateinit var arenaManager: ArenaManager
    private lateinit var sidebarService: SidebarService

    override fun onEnable() {
        instance = this
        initStorage()
        initHooks()
        initSidebar()
        registerCommands()
        registerListeners()
        initDisasters()
        initBungee()
    }

    override fun onDisable() {
    }

    private fun initHooks () {
        // World Edit (FAWE)
        val worldEditPlugin = server.pluginManager.getPlugin("WorldEdit") as WorldEditPlugin?
        if (worldEditPlugin == null) {
            logger.severe("WorldEdit plugin not found! Disabling...")
            server.pluginManager.disablePlugin(this)
        }

        // Arena Manager
        arenaManager = ArenaManager(worldEditPlugin)

        // PlaceholderAPI
        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook(arenaManager).register()
        }
    }

    private fun initStorage(){
        Filer.createFolders()
        FileManager.initialize()
        Lobby.setLocation()
        Data.load()
    }

    private fun initSidebar(){
        sidebarService = SidebarService(arenaManager)
    }

    private fun registerCommands() {
        val lamp: Lamp<BukkitCommandActor> = BukkitLamp.builder(this)
            .build()
        lamp.register(ArenaCommand(arenaManager), DisastersCommand(arenaManager, sidebarService))
    }

    private fun registerListeners(){
        server.pluginManager.registerSuspendingEvents(PlayerJoinListener(), this)
        server.pluginManager.registerSuspendingEvents(PlayerDamageListener(arenaManager), this)
        server.pluginManager.registerSuspendingEvents(PlayerDeathListener(arenaManager), this)
        server.pluginManager.registerEvents(ExplosionListener(arenaManager), this)
        server.pluginManager.registerEvents(ItemPickupListener(arenaManager), this)
        server.pluginManager.registerEvents(PlayerLeaveListener(arenaManager), this)
        server.pluginManager.registerEvents(BlockBreakListener(arenaManager), this)
        server.pluginManager.registerEvents(BlockPlaceListener(arenaManager), this)
        server.pluginManager.registerEvents(HealthRegenListener(arenaManager), this)
        server.pluginManager.registerEvents(PlayerMoveListener(arenaManager), this)
        server.pluginManager.registerEvents(PlayerJumpListener(arenaManager), this)
    }

    private fun initDisasters() {
        DisasterTask().runTaskTimer(this, 0, 20)
    }

    private fun initBungee() {
        if (FileManager.get("config")!!.getBoolean("bungee.enabled", false)) {
            server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
            logger.info("BungeeCord mode enabled! Players will be sent to server: ${FileManager.get("config")!!.getString("bungee.server", "lobby")}")
        }
    }

}