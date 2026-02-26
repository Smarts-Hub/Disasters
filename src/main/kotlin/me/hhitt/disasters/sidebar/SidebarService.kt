package me.hhitt.disasters.sidebar

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.storage.file.FileManager

/**
 * SidebarService is responsible for updating the sidebar for players.
 * It updates the sidebars based on the game state.
 *
 * @param arenaManager The ArenaManager instance used to manage arenas.
 */

class SidebarService(private val arenaManager: ArenaManager) {

    private val plugin = Disasters.getInstance()
    private val sidebarManager = SidebarManager()
    private var sidebarTask: SidebarTask? = null

    init {
        updateSidebar()
    }

    fun updateSidebar() {
        val isScoreboardEnabled = FileManager.get("config")!!.getBoolean("enable-scoreboard")

        if (isScoreboardEnabled) {
            if (sidebarTask == null || sidebarTask!!.isCancelled) {
                sidebarTask = SidebarTask(arenaManager, sidebarManager)
                sidebarTask!!.runTaskTimer(plugin, 0, 20L)
            }
        } else {
            sidebarTask?.cancel()
            sidebarTask = null
        }
    }

    fun shutdown() {
        sidebarTask?.cancel()
    }
}