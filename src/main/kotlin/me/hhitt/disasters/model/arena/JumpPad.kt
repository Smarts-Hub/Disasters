package me.hhitt.disasters.model.arena

import org.bukkit.Location

data class JumpPad(
    val id: String,
    val location: Location,
    val powerY: Double,
    val powerForward: Double,
    val cooldownTicks: Long
)
