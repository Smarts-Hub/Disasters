package me.hhitt.disasters.game.modification

import me.hhitt.disasters.arena.Arena

interface GameModification {
    val id: String
    val displayName: String
    fun start(arena: Arena)
    fun pulse(arena: Arena, time: Int)
    fun stop(arena: Arena)
}
