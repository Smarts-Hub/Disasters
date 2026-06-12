package me.hhitt.disasters.model.arena;

import org.bukkit.Location;

import java.util.Objects;

public final class JumpPad {
    private final String id;
    private final Location location;
    private final double powerY;
    private final double powerForward;
    private final long cooldownTicks;

    public JumpPad(final String id, final Location location, final double powerY, final double powerForward, final long cooldownTicks) {
        this.id = Objects.requireNonNull(id, "id");
        this.location = Objects.requireNonNull(location, "location");
        this.powerY = powerY;
        this.powerForward = powerForward;
        this.cooldownTicks = cooldownTicks;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public double getPowerY() {
        return powerY;
    }

    public double getPowerForward() {
        return powerForward;
    }

    public long getCooldownTicks() {
        return cooldownTicks;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof JumpPad)) {
            return false;
        }
        final JumpPad jumpPad = (JumpPad) object;
        return Double.compare(jumpPad.powerY, powerY) == 0
            && Double.compare(jumpPad.powerForward, powerForward) == 0
            && cooldownTicks == jumpPad.cooldownTicks
            && id.equals(jumpPad.id)
            && location.equals(jumpPad.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, powerY, powerForward, cooldownTicks);
    }

    @Override
    public String toString() {
        return "JumpPad(id=" + id
            + ", location=" + location
            + ", powerY=" + powerY
            + ", powerForward=" + powerForward
            + ", cooldownTicks=" + cooldownTicks
            + ')';
    }
}
