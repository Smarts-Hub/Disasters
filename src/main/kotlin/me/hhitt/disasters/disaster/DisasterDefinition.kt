package me.hhitt.disasters.disaster

import kotlin.reflect.KClass

data class DisasterDefinition(
    val id: String,
    val displayName: String,
    val type: KClass<out Disaster>,
    val factory: () -> Disaster,
    val incompatibleWith: Set<String> = emptySet()
)
