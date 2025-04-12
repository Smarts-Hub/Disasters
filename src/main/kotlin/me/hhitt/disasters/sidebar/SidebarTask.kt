package me.hhitt.disasters.sidebar

import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 * SidebarTask is a scheduled task that updates the sidebar for all online players.
 * It runs every second to ensure that the sidebar is always up to date with the current arena state.
 *
 * @param arenaManager The ArenaManager instance used to get the current arena state for each player.
 * @param sidebarManager The SidebarManager instance used to update the sidebar for each player.
 */

class SidebarTask(private val arenaManager: ArenaManager, private val sidebarManager: SidebarManager) : BukkitRunnable() {

    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player: Player ->
            arenaManager.getArena(player)?.let { arena ->
                sidebarManager.updateSidebar(player, arena.state)
            } ?: sidebarManager.updateSidebar(player, null)
        }
    }

}
