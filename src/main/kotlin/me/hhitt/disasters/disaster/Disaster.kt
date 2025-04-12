package me.hhitt.disasters.disaster

import me.hhitt.disasters.arena.Arena

interface Disaster {
    fun start(arena: Arena)
    fun pulse(time: Int)
    fun stop(arena: Arena)
}