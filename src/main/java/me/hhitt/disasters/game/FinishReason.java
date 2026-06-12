package me.hhitt.disasters.game;

public enum FinishReason {

    COUNTDOWN_CANCELLED(false),
    TIME_LIMIT(true),
    ALIVE_THRESHOLD(true),
    ADMIN_STOP(true),
    PLUGIN_DISABLE(false);

    private final boolean recordOutcome;

    FinishReason(final boolean recordOutcome) {
        this.recordOutcome = recordOutcome;
    }

    public boolean shouldRecordOutcome() {
        return recordOutcome;
    }
}
