package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.Particle
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class Covid19 : Disaster, TriggerTrackedDisaster {

    override var triggerCount: Int = 0
        private set

    private val arenas = mutableListOf<Arena>()
    private val infected = ConcurrentHashMap<Arena, MutableSet<UUID>>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        val first = arena.alive.randomOrNull() ?: return
        infected[arena] = mutableSetOf(first.uniqueId)
        Notify.disaster(arena, "covid-19")
        triggerCount = 1
    }

    override fun pulse(time: Int) {
        if (time % 2 != 0) return
        arenas.toList().forEach { arena ->
            val set = infected[arena] ?: return@forEach
            val newInfections = mutableSetOf<UUID>()
            arena.alive.toList().forEach { player ->
                if (player.uniqueId in set) {
                    player.world.spawnParticle(Particle.HAPPY_VILLAGER, player.location.add(0.0, 1.0, 0.0), 5, 0.4, 0.4, 0.4)
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 60, 0, true, false))
                    DeathMessageService.mark(player, "covid-19")
                } else {
                    val nearInfected = arena.alive.any { other ->
                        other.uniqueId in set && other.location.distanceSquared(player.location) <= 9.0
                    }
                    if (nearInfected && Random.nextInt(100) < 60) {
                        newInfections.add(player.uniqueId)
                    }
                }
            }
            set.addAll(newInfections)
        }
    }

    override fun stop(arena: Arena) {
        val set = infected.remove(arena) ?: return
        arenas.remove(arena)
        arena.alive.toList().forEach { player ->
            if (player.uniqueId in set) {
                player.removePotionEffect(PotionEffectType.SLOWNESS)
                player.removePotionEffect(PotionEffectType.WEAKNESS)
            }
        }
    }
}
