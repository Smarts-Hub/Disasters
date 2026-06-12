package me.hhitt.disasters.disaster;

import me.hhitt.disasters.arena.Arena;

public interface Disaster {
    void start(Arena arena);

    /**
     * @param time elapsed whole seconds since this activation's {@link #start(Arena)} announcement;
     *             the first scheduled pulse is 1 and values are monotonic until stop
     */
    void pulse(int time);

    void stop(Arena arena);
}
