package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Msg
import me.hhitt.disasters.util.Notify
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Murder : Disaster {

    private val players = ConcurrentHashMap<UUID, Player>()
    private val murderers = ConcurrentHashMap.newKeySet<UUID>()

    override fun start(arena: Arena) {
        arena.playing.forEach { player ->
            players[player.uniqueId] = player
        }
        addMurder()
        Notify.disaster(arena, "murder")
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
        players.forEach { (uuid, player) ->
            if (murderers.contains(uuid)) {
                player.inventory.setItem(5, ItemStack(Material.AIR))
            }
        }
        players.clear()
        murderers.clear()
    }

    private fun addMurder() {
        val remaining = players.keys.filterNot { it in murderers }
        if (remaining.isEmpty()) return

        val selectedId = remaining.random()
        murderers.add(selectedId)

        val player = players[selectedId] ?: return
        player.inventory.setItem(5, ItemStack.of(Material.WOODEN_SWORD))
        Msg.send(player, "now-murder")
    }

    fun isMurder(player: Player): Boolean {
        return player.uniqueId in murderers
    }
}