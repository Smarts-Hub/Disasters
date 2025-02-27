package me.hhitt.disasters.disaster

import me.hhitt.disasters.arena.Arena

interface Disaster {
    fun start(arena: Arena)
    fun pulse()
    fun stop(arena: Arena)
}