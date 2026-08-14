package me.hhitt.disasters.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StartBlockReasonTest {

    @Test
    void countdownBlocksBelowConfiguredMinimum() {
        assertEquals(StartBlockReason.BELOW_MINIMUM_PLAYERS, StartBlockReason.forCountdown(1, 2, 0));
    }

    @Test
    void countdownReportsEndThresholdWhenMinimumIsMetButGameWouldAlreadyBeOver() {
        assertEquals(StartBlockReason.PLAYER_COUNT_AT_OR_BELOW_END_THRESHOLD, StartBlockReason.forCountdown(2, 2, 2));
    }

    @Test
    void countdownIsReadyWhenBothRequirementsPass() {
        assertEquals(StartBlockReason.READY, StartBlockReason.forCountdown(2, 2, 1));
    }

    @Test
    void forceStartBypassesNormalMinimumRequirement() {
        assertEquals(StartBlockReason.READY, StartBlockReason.forForceStart(1, 0));
    }

    @Test
    void forceStartBlocksRosterAlreadyAtEndThreshold() {
        assertEquals(StartBlockReason.PLAYER_COUNT_AT_OR_BELOW_END_THRESHOLD, StartBlockReason.forForceStart(1, 1));
    }
}
