package me.hhitt.disasters.arena;

import java.util.Objects;

public final class ArenaDisasterSettings {

    private final int floodStartY;
    private final int lavaRisingStartY;

    public ArenaDisasterSettings(final int floodStartY, final int lavaRisingStartY) {
        this.floodStartY = floodStartY;
        this.lavaRisingStartY = lavaRisingStartY;
    }

    public int getFloodStartY() {
        return floodStartY;
    }

    public int getLavaRisingStartY() {
        return lavaRisingStartY;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ArenaDisasterSettings)) {
            return false;
        }
        final ArenaDisasterSettings that = (ArenaDisasterSettings) object;
        return floodStartY == that.floodStartY && lavaRisingStartY == that.lavaRisingStartY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floodStartY, lavaRisingStartY);
    }

    @Override
    public String toString() {
        return "ArenaDisasterSettings{" +
            "floodStartY=" + floodStartY +
            ", lavaRisingStartY=" + lavaRisingStartY +
            '}';
    }
}
