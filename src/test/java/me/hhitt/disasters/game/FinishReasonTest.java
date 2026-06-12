package me.hhitt.disasters.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinishReasonTest {

    @Test
    void shouldRecordOutcomeForTimeLimit() {
        assertTrue(FinishReason.TIME_LIMIT.shouldRecordOutcome());
    }

    @Test
    void shouldRecordOutcomeForAliveThreshold() {
        assertTrue(FinishReason.ALIVE_THRESHOLD.shouldRecordOutcome());
    }

    @Test
    void shouldRecordOutcomeForAdminStop() {
        assertTrue(FinishReason.ADMIN_STOP.shouldRecordOutcome());
    }

    @Test
    void shouldNotRecordOutcomeForCountdownCancelled() {
        assertFalse(FinishReason.COUNTDOWN_CANCELLED.shouldRecordOutcome());
    }

    @Test
    void shouldNotRecordOutcomeForPluginDisable() {
        assertFalse(FinishReason.PLUGIN_DISABLE.shouldRecordOutcome());
    }
}
