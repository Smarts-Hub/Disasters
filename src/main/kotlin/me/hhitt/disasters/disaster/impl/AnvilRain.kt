package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.disaster.TriggerTrackedDisaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import kotlin.random.Random

class AnvilRain : Disaster, TriggerTrackedDisaster {

    override var triggerCount: Int = 0
        private set

    private val arenas = mutableListOf<Arena>()
    private val anvils = mutableMapOf<Arena, MutableList<FallingBlock>>()

    override fun start(arena: Arena) {
        arenas.add(arena)
        anvils[arena] = mutableListOf()
        Notify.disaster(arena, "anvil-rain")
    }

    override fun pulse(time: Int) {
        if (time % 2 != 0) return
        arenas.toList().forEach { arena ->
            val list = anvils[arena] ?: return@forEach
            list.removeAll { it.isDead }

            arena.alive.toList().forEach { player ->
                val target = player.location.clone().add(
                    Random.nextDouble(-6.0, 6.0), 0.0, Random.nextDouble(-6.0, 6.0)
                )
                if (!arena.borderService.isLocationInArena(target)) return@forEach
                val spawn = target.clone().add(0.0, Random.nextDouble(20.0, 28.0), 0.0)
                val anvil = spawn.world.spawn(spawn, FallingBlock::class.java)
                anvil.blockData = Material.ANVIL.createBlockData()
                anvil.dropItem = false
                anvil.setHurtEntities(true)
                anvil.setDamagePerBlock(8f)
                anvil.setMaxDamage(40)
                list.add(anvil)
                triggerCount++
            }
        }
    }

    override fun stop(arena: Arena) {
        anvils.remove(arena)?.forEach { if (!it.isDead) it.remove() }
        arenas.remove(arena)
    }
}
