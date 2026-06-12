package me.hhitt.disasters.disaster;

import me.hhitt.disasters.arena.Arena;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DisasterRegistryTimingTest {

    @Test
    void firstPulseStartsAtOne() {
        final RecordingDisaster disaster = new RecordingDisaster();
        final ActiveDisaster active = active(disaster, 30, 0);

        final boolean removed = DisasterRegistry.pulseOne(arenaWith(disaster), active);

        assertFalse(removed);
        assertEquals(1, disaster.pulseCount());
        assertEquals(Integer.valueOf(1), disaster.pulseAt(0));
    }

    @Test
    void activationsTrackElapsedIndependently() {
        final RecordingDisaster first = new RecordingDisaster();
        final RecordingDisaster second = new RecordingDisaster();
        final ActiveDisaster firstActive = active(first, 30, 0);
        final ActiveDisaster secondActive = active(second, 30, 0);

        DisasterRegistry.pulseOne(arenaWith(first), firstActive);
        DisasterRegistry.pulseOne(arenaWith(first), firstActive);
        DisasterRegistry.pulseOne(arenaWith(second), secondActive);

        assertEquals(Integer.valueOf(2), first.pulseAt(1));
        assertEquals(Integer.valueOf(1), second.pulseAt(0));
    }

    @Test
    void durationStopsAfterFinalPulse() {
        final RecordingDisaster disaster = new RecordingDisaster();
        final Arena arena = arenaWith(disaster);
        final ActiveDisaster active = active(disaster, 2, 0);

        assertFalse(DisasterRegistry.pulseOne(arena, active));
        assertTrue(DisasterRegistry.pulseOne(arena, active));

        assertEquals(2, disaster.pulseCount());
        assertEquals(Integer.valueOf(2), disaster.pulseAt(1));
        assertEquals(1, disaster.stopCount());
    }

    @Test
    void nukeStyleThresholdIsRelativeToActivation() {
        final ThresholdDisaster disaster = new ThresholdDisaster(20);
        final Arena arena = arenaWith(disaster);
        final ActiveDisaster active = active(disaster, 60, 1);

        for (int i = 0; i < 19; i++) {
            assertFalse(DisasterRegistry.pulseOne(arena, active));
        }

        assertTrue(DisasterRegistry.pulseOne(arena, active));
        assertEquals(Integer.valueOf(20), disaster.pulseAt(19));
        assertEquals(1, disaster.stopCount());
    }

    private static ActiveDisaster active(final Disaster disaster, final int durationSeconds, final int maxTriggers) {
        return new ActiveDisaster(
            new DisasterDefinition("test", "Test", disaster.getClass(), () -> disaster),
            disaster,
            durationSeconds,
            maxTriggers
        );
    }

    private static Arena arenaWith(final Disaster disaster) {
        final Arena arena = mock(Arena.class);
        final List<Disaster> disasters = new ArrayList<Disaster>();
        disasters.add(disaster);
        when(arena.getName()).thenReturn("test");
        when(arena.getDisasters()).thenReturn(disasters);
        return arena;
    }

    private static class RecordingDisaster implements Disaster {
        private final List<Integer> pulses = new ArrayList<Integer>();
        private int stops;

        @Override
        public void start(final Arena arena) {
        }

        @Override
        public void pulse(final int time) {
            pulses.add(Integer.valueOf(time));
        }

        @Override
        public void stop(final Arena arena) {
            stops++;
        }

        final int pulseCount() {
            return pulses.size();
        }

        final Integer pulseAt(final int index) {
            return pulses.get(index);
        }

        final int stopCount() {
            return stops;
        }
    }

    private static final class ThresholdDisaster extends RecordingDisaster implements TriggerTrackedDisaster {
        private final int threshold;
        private int triggerCount;

        private ThresholdDisaster(final int threshold) {
            this.threshold = threshold;
        }

        @Override
        public void pulse(final int time) {
            super.pulse(time);
            if (time == threshold) {
                triggerCount++;
            }
        }

        @Override
        public int getTriggerCount() {
            return triggerCount;
        }
    }
}
