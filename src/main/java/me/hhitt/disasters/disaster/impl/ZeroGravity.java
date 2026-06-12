package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.CopyOnWriteArrayList;

public class ZeroGravity implements Disaster {

    private final CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();
    private int count = 0;

    @Override
    public void start(Arena arena) {
        for (Player player : arena.getPlaying()) {
            players.add(player);
            player.addPotionEffect(
                new PotionEffect(
                    PotionEffectType.LEVITATION,
                    20 * 5,
                    1,
                    true,
                    false
                )
            );
        }

        Notify.disaster(arena, "zero-gravity");
    }

    @Override
    public void pulse(int time) {
        if (count > 110) return;

        if (time % 11 != 0) return;

        for (Player player : players) {
            player.addPotionEffect(
                new PotionEffect(
                    PotionEffectType.LEVITATION,
                    20 * 5,
                    1,
                    true,
                    false
                )
            );
        }

        count++;
    }

    @Override
    public void stop(Arena arena) {
        for (Player player : arena.getPlaying()) {
            players.remove(player);
        }
    }
}
