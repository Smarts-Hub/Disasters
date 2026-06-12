package me.hhitt.disasters.disaster;

import java.util.Objects;

public final class ActiveDisaster {
    private final DisasterDefinition definition;
    private final Disaster disaster;
    private int elapsedSeconds;
    private final int durationSeconds;
    private final int maxTriggers;

    public ActiveDisaster(final DisasterDefinition definition, final Disaster disaster, final int durationSeconds, final int maxTriggers) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.disaster = Objects.requireNonNull(disaster, "disaster");
        this.elapsedSeconds = 0;
        this.durationSeconds = durationSeconds;
        this.maxTriggers = maxTriggers;
    }

    public DisasterDefinition getDefinition() {
        return definition;
    }

    public Disaster getDisaster() {
        return disaster;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public int advanceSecond() {
        elapsedSeconds++;
        return elapsedSeconds;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getMaxTriggers() {
        return maxTriggers;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ActiveDisaster)) {
            return false;
        }
        final ActiveDisaster that = (ActiveDisaster) object;
        return elapsedSeconds == that.elapsedSeconds
            && durationSeconds == that.durationSeconds
            && maxTriggers == that.maxTriggers
            && definition.equals(that.definition)
            && disaster.equals(that.disaster);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definition, disaster, elapsedSeconds, durationSeconds, maxTriggers);
    }

    @Override
    public String toString() {
        return "ActiveDisaster(definition=" + definition
            + ", disaster=" + disaster
            + ", elapsedSeconds=" + elapsedSeconds
            + ", durationSeconds=" + durationSeconds
            + ", maxTriggers=" + maxTriggers
            + ')';
    }
}
