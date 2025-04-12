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
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary
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
    private lateinit var scoreboardLibrary: ScoreboardLibrary
    private lateinit var sidebarService: SidebarService

    override fun onEnable() {
        instance = this
        initStorage()
        initHooks()
        initSidebar()
        registerCommands()
        registerListeners()
        initDisasters()
    }

    override fun onDisable() {
        scoreboardLibrary.close()
    }

    private fun initHooks () {
        // World Edit (FAWE)
        val worldEditPlugin = server.pluginManager.getPlugin("WorldEdit") as WorldEditPlugin?
        if (worldEditPlugin == null) {
            logger.severe("WorldEdit plugin not found! Disabling...")
            server.pluginManager.disablePlugin(this)
        }

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

    private fun registerCommands() {
        val lamp: Lamp<BukkitCommandActor> = BukkitLamp.builder(this)
            .build()
        lamp.register(ArenaCommand(arenaManager), DisastersCommand(arenaManager, sidebarService))
    }

    private fun registerListeners(){
        server.pluginManager.registerSuspendingEvents(PlayerJoinListener(), this)
        server.pluginManager.registerSuspendingEvents(PlayerDamageListener(arenaManager), this)
        server.pluginManager.registerSuspendingEvents(PlayerDeathListener(arenaManager), this)
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

    private fun initSidebar(){
        try {
            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(this)
        } catch (e: NoPacketAdapterAvailableException) {
            scoreboardLibrary = NoopScoreboardLibrary()
            logger.warning("No scoreboard packet adapter available!")
        }
        sidebarService = SidebarService(scoreboardLibrary, arenaManager)
    }

}
