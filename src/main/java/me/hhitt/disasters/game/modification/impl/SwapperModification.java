package me.hhitt.disasters.game.modification.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.game.modification.GameModification;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwapperModification implements GameModification {

    private int elapsed;
    private int lastSwapTick = -1;
    private int interval = 10;

    @Override
    public String getId() {
        return "swapper";
    }

    @Override
    public String getDisplayName() {
        return "Swapper";
    }

    @Override
    public void start(final Arena arena) {
        elapsed = 0;
        lastSwapTick = -1;
        final FileConfiguration config = FileManager.get("config");
        interval = config != null ? config.getInt("game-modifications.swapper.interval-seconds", 10) : 10;
        Notify.disaster(arena, "swapper");
    }

    @Override
    public void pulse(final Arena arena, final int time) {
        if (interval <= 0) return;
        if (time - lastSwapTick >= interval) {
            final List<Player> players = new ArrayList<Player>(arena.getAlive());
            Collections.shuffle(players);
            for (int i = 0; i < players.size() - 1; i += 2) {
                final Player a = players.get(i);
                final Player b = players.get(i + 1);
                final Location locA = a.getLocation();
                final Location locB = b.getLocation();
                a.teleport(locB);
                b.teleport(locA);
            }
            lastSwapTick = time;
        }
    }

    @Override
    public void stop(final Arena arena) {
    }
}
