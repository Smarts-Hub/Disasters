package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener(private val arenaManager: ArenaManager): Listener {

     @EventHandler
     fun onBlockPlace(event: BlockPlaceEvent) {
         arenaManager.getArena(event.player)?.let { arena ->
             if(arena.isWaiting()) {
                 event.isCancelled = true
             }
         }
     }
}