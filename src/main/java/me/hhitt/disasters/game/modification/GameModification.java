package me.hhitt.disasters.game.modification;

import me.hhitt.disasters.arena.Arena;

public interface GameModification {
    String getId();

    String getDisplayName();

    void start(Arena arena);

    void pulse(Arena arena, int time);

    void stop(Arena arena);
}
