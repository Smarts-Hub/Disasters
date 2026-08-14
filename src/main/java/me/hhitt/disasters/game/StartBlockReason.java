package me.hhitt.disasters.game;

public enum StartBlockReason {

    READY,
    BELOW_MINIMUM_PLAYERS,
    PLAYER_COUNT_AT_OR_BELOW_END_THRESHOLD;

    public static StartBlockReason forCountdown(final int currentPlayers, final int minimumPlayers, final int aliveToEnd) {
        if (currentPlayers < minimumPlayers) {
            return BELOW_MINIMUM_PLAYERS;
        }
        if (currentPlayers <= aliveToEnd) {
            return PLAYER_COUNT_AT_OR_BELOW_END_THRESHOLD;
        }
        return READY;
    }

    public static StartBlockReason forForceStart(final int currentPlayers, final int aliveToEnd) {
        if (currentPlayers <= aliveToEnd) {
            return PLAYER_COUNT_AT_OR_BELOW_END_THRESHOLD;
        }
        return READY;
    }
}
