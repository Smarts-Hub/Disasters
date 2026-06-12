package me.hhitt.disasters.disaster

data class ActiveDisaster(
    val definition: DisasterDefinition,
    val disaster: Disaster,
    var elapsedSeconds: Int = 0,
    val durationSeconds: Int,
    val maxTriggers: Int
)
