package me.hhitt.disasters.game

/**
 * Represents the game mode for a disaster round.
 * PVE = only environmental disasters (no player-vs-player)
 * PVP = all disasters including PvP ones
 */
enum class GameMode {
    PVP,
    PVE
}