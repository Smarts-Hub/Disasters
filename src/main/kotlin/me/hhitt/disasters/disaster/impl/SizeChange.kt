package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.util.Msg
import me.hhitt.disasters.util.Notify
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class SizeChange : Disaster, TriggerTrackedDisaster {

    override var triggerCount: Int = 0
        private set

    private val arenas = mutableListOf<Arena>()
    private val oldScales = ConcurrentHashMap<UUID, Double>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        arena.alive.toList().forEach { player ->
            val attr = player.getAttribute(Attribute.SCALE)
            if (attr != null) {
                oldScales[player.uniqueId] = attr.baseValue
                val newScale = if (Random.nextBoolean()) 0.5 else 2.0
                attr.baseValue = newScale
                player.sendMessage(Msg.parse("<yellow>Size change: <white>${newScale}x"))
            }
        }
        triggerCount = 1
        Notify.disaster(arena, "size-change")
    }

    override fun pulse(time: Int) {
    }

    override fun stop(arena: Arena) {
        arenas.remove(arena)
        val toRemove = mutableListOf<UUID>()
        for ((uuid, scale) in oldScales) {
            val player = org.bukkit.Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline) {
                val attr = player.getAttribute(Attribute.SCALE)
                if (attr != null) {
                    attr.baseValue = scale
                }
            }
            toRemove.add(uuid)
        }
        toRemove.forEach { oldScales.remove(it) }
    }
}
