package me.hhitt.disasters.game.modification.impl

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.modification.GameModification
import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Notify
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class OneHeartModification : GameModification {

    override val id: String = "one-heart"
    override val displayName: String = "One Heart"

    private val oldMaxHealth = ConcurrentHashMap<UUID, Double>()

    override fun start(arena: Arena) {
        val health = FileManager.get("config")?.getDouble("game-modifications.one-heart.health", 2.0) ?: 2.0
        arena.playing.toList().forEach { player ->
            val attr = player.getAttribute(Attribute.MAX_HEALTH)
            if (attr != null) {
                oldMaxHealth.putIfAbsent(player.uniqueId, attr.baseValue)
                attr.baseValue = health
                if (player.health > health) player.health = health
                player.absorptionAmount = 0.0
            }
        }
        Notify.disaster(arena, "one-heart")
    }

    override fun pulse(arena: Arena, time: Int) {
        val health = FileManager.get("config")?.getDouble("game-modifications.one-heart.health", 2.0) ?: 2.0
        arena.alive.toList().forEach { player ->
            if (player.health > health) player.health = health
            if (player.absorptionAmount > 0.0) player.absorptionAmount = 0.0
        }
    }

    override fun stop(arena: Arena) {
        val toRemove = mutableListOf<UUID>()
        for ((uuid, value) in oldMaxHealth) {
            val player = org.bukkit.Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline) {
                val attr = player.getAttribute(Attribute.MAX_HEALTH)
                if (attr != null) {
                    attr.baseValue = value
                    if (player.health > value) player.health = value
                }
            }
            toRemove.add(uuid)
        }
        toRemove.forEach { oldMaxHealth.remove(it) }
    }
}
