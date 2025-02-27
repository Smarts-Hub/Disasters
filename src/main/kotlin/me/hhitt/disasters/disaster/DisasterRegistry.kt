package me.hhitt.disasters.disaster

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.impl.*

object DisasterRegistry {
    private val activeDisasters = mutableMapOf<Arena, MutableList<Disaster>>()
    private val disasterClasses = listOf(
        AcidRain::class,
        Apocalypse::class,
        ExplosiveSheep::class,
        FloorIsLava::class,
        Grounded::class,
        Lightning::class,
        OneHearth::class,
        Swap::class,
        WorldBorder::class
    )

    fun addRandomDisaster(arena: Arena) {
        val maxDisasters = arena.maxDisasters
        val currentDisasters = activeDisasters.getOrPut(arena) { mutableListOf() }

        if (currentDisasters.size >= maxDisasters) {
            val disasterToRemove = currentDisasters.removeAt(0)
            disasterToRemove.stop(arena)
        }

        val availableDisasters = disasterClasses.filter { cls ->
            currentDisasters.none { it::class == cls }
        }

        if (availableDisasters.isNotEmpty()) {
            val disasterClass = availableDisasters.random()
            val disaster = disasterClass.constructors.first().call()
            disaster.start(arena)
            currentDisasters.add(disaster)
        }
    }

    fun pulseAll() {
        activeDisasters.forEach { (arena, disasters) ->
            disasters.forEach { it.pulse() }
        }
    }

    fun removeDisasters(arena: Arena) {
        activeDisasters[arena]?.forEach { it.stop(arena) }
        activeDisasters.remove(arena)
    }
}
