package me.hhitt.disasters.disaster;

import me.hhitt.disasters.arena.Arena;

public interface Disaster {
    void start(Arena arena);

    void pulse(int time);

    void stop(Arena arena);
}
