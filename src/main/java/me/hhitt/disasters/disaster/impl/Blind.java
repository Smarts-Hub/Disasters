package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Blind implements Disaster {

    @Override
    public void start(Arena arena) {
        for (Player player : arena.getPlaying()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 20, 0, true, false));
        }
        Notify.disaster(arena, "blind");
    }

    @Override
    public void pulse(int time) {
    }

    @Override
    public void stop(Arena arena) {
        for (Player player : arena.getPlaying()) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }
}
