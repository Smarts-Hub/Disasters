package me.hhitt.disasters.sidebar

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.file.FileManager
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary

/**
 * SidebarService is responsible for updating the sidebar for players.
 * It updates the sidebars based on the game state.
 *
 * @param scoreboardLibrary The ScoreboardLibrary instance used to create sidebars.
 * @param arenaManager The ArenaManager instance used to manage arenas.
 */

class SidebarService(scoreboardLibrary: ScoreboardLibrary, arenaManager: ArenaManager) {

    private val plugin = Disasters.getInstance()
    private var sidebarManager: SidebarManager = SidebarManager(scoreboardLibrary)
    private var sidebarTask: SidebarTask = SidebarTask(arenaManager, sidebarManager)

    init {
        if(FileManager.get("config")!!.getBoolean("enable-scoreboard")) {
            sidebarTask.runTaskTimerAsynchronously(plugin, 0, 20L)
        }
    }

    fun updateSidebar() {
        if(FileManager.get("config")!!.getBoolean("enable-scoreboard")) {
            if(!sidebarTask.isCancelled) return
            sidebarTask.runTaskTimerAsynchronously(plugin, 0, 20L)
            return
        }
        sidebarTask.cancel()
    }
}