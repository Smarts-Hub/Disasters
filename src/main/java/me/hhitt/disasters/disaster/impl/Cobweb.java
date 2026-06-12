package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.concurrent.CopyOnWriteArrayList;

public class Cobweb implements Disaster {

    private final CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();

    @Override
    public void start(Arena arena) {
        for (Player player : arena.getPlaying()) {
            players.add(player);
        }
        Notify.disaster(arena, "cobweb");
    }

    @Override
    public void pulse(int time) {
        if (time % 5 != 0) return;
        for (Player player : players) {
            if (player.getLocation().getBlock().getType() != Material.COBWEB) {
                setInCobweb(player);
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        for (Player player : arena.getPlaying()) {
            players.remove(player);
        }
    }

    public void setInCobweb(Player player) {
        player.getLocation().getBlock().setType(Material.COBWEB);
    }
}
