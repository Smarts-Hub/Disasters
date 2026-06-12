package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.entity.Player;

import java.util.concurrent.CopyOnWriteArrayList;

public class Grounded implements Disaster {

    private final CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();

    @Override
    public void start(Arena arena) {
        for (Player player : arena.getPlaying()) {
            players.add(player);
        }
        Notify.disaster(arena, "grounded");
    }

    @Override
    public void pulse(int time) {
    }

    @Override
    public void stop(Arena arena) {
        for (Player player : arena.getPlaying()) {
            players.remove(player);
        }
    }

    public boolean isGrounded(Player player) {
        return players.contains(player);
    }
}
