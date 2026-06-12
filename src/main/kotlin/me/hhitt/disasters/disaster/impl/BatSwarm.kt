package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.service.DeathMessageService
import me.hhitt.disasters.util.Notify
import org.bukkit.entity.Bat
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class BatSwarm : Disaster {

    private val arenas = mutableListOf<Arena>()
    private val bats = mutableMapOf<Arena, MutableList<Bat>>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        bats[arena] = mutableListOf()
        spawnWave(arena, Random.nextInt(8, 13))
        Notify.disaster(arena, "bat-swarm")
    }

    override fun pulse(time: Int) {
        if (time > 0 && time % 8 == 0) {
            arenas.toList().forEach { arena ->
                spawnWave(arena, Random.nextInt(8, 13))
            }
        }
        arenas.toList().forEach { arena ->
            val list = bats[arena] ?: return@forEach
            list.removeAll { it.isDead }
            arena.alive.toList().forEach { player ->
                val nearby = list.count { it.location.distanceSquared(player.location) <= 16.0 }
                if (nearby > 0) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, false))
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 0, true, false))
                    DeathMessageService.mark(player, "bat-swarm")
                }
            }
        }
    }

    override fun stop(arena: Arena) {
        bats.remove(arena)?.forEach { if (!it.isDead) it.remove() }
        arenas.remove(arena)
    }

    private fun spawnWave(arena: Arena, count: Int) {
        val list = bats[arena] ?: return
        arena.alive.toList().forEach { player ->
            repeat(count) {
                val spawn = player.location.clone().add(
                    Random.nextDouble(-4.0, 4.0),
                    Random.nextDouble(2.0, 6.0),
                    Random.nextDouble(-4.0, 4.0)
                )
                if (!arena.borderService.isLocationInArena(spawn)) return@repeat
                val bat = spawn.world.spawn(spawn, Bat::class.java)
                list.add(bat)
            }
        }
    }
}
