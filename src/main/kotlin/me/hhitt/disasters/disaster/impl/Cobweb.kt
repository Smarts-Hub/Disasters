package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Player
 import java.util.concurrent.CopyOnWriteArrayList

class Cobweb : Disaster {

    private val players = CopyOnWriteArrayList<Player>()

    override fun start(arena: Arena) {
        arena.playing.forEach { players.add(it) }
        Notify.disaster(arena, "cobweb")
    }

    override fun pulse(time: Int) {
        if(time % 5 != 0) return
        players.forEach { player ->
            if (player.location.block.type != org.bukkit.Material.COBWEB) {
                setInCobweb(player)
            }
        }
    }

    override fun stop(arena: Arena) {
        arena.playing.forEach { players.remove(it) }
    }

    fun setInCobweb(player: Player) {
        player.location.block.type = org.bukkit.Material.COBWEB
    }
}