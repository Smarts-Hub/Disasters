package me.hhitt.disasters.game.modification

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterSettings
import me.hhitt.disasters.game.modification.impl.OneHeartModification
import me.hhitt.disasters.game.modification.impl.SwapperModification
import org.bukkit.Material

object GameModificationRegistry {

    private val definitions: List<GameModificationDefinition> = listOf(
        GameModificationDefinition(
            "one-heart",
            "One Heart",
            Material.RED_DYE,
            listOf("Everyone has one heart.", "No regen allowed.")
        ) { OneHeartModification() },
        GameModificationDefinition(
            "swapper",
            "Swapper",
            Material.ENDER_PEARL,
            listOf("Players swap positions", "during the match.")
        ) { SwapperModification() }
    )

    fun all(): List<GameModificationDefinition> = definitions

    fun enabledDefinitions(arena: Arena): List<GameModificationDefinition> {
        return definitions.filter { DisasterSettings.isGameModificationEnabled(arena, it.id) }
    }

    fun start(arena: Arena, ids: Collection<String>) {
        stop(arena)
        val enabled = enabledDefinitions(arena).map { it.id }.toSet()
        ids.filter { it in enabled }.forEach { id ->
            val def = definitions.firstOrNull { it.id == id } ?: return@forEach
            val mod = def.factory()
            mod.start(arena)
            arena.activeGameModifications.add(mod)
        }
    }

    fun pulse(arena: Arena, time: Int) {
        arena.activeGameModifications.toList().forEach { it.pulse(arena, time) }
    }

    fun stop(arena: Arena) {
        arena.activeGameModifications.toList().forEach { it.stop(arena) }
        arena.activeGameModifications.clear()
    }

    fun isActive(arena: Arena, id: String): Boolean {
        return arena.activeGameModifications.any { it.id == id }
    }

    fun displayNames(arena: Arena): List<String> {
        return arena.activeGameModifications.map { it.displayName }
    }
}
