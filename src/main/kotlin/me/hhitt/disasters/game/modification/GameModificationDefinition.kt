package me.hhitt.disasters.game.modification

import org.bukkit.Material

data class GameModificationDefinition(
    val id: String,
    val displayName: String,
    val material: Material,
    val description: List<String>,
    val factory: () -> GameModification
)
