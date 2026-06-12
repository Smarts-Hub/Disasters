package me.hhitt.disasters.game.modification.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.game.modification.GameModification;
import me.hhitt.disasters.util.Notify;

public final class PvpModification implements GameModification {

    @Override
    public String getId() {
        return "pvp";
    }

    @Override
    public String getDisplayName() {
        return "PvP";
    }

    @Override
    public void start(final Arena arena) {
        Notify.disaster(arena, "pvp");
    }

    @Override
    public void pulse(final Arena arena, final int time) {
    }

    @Override
    public void stop(final Arena arena) {
    }
}
