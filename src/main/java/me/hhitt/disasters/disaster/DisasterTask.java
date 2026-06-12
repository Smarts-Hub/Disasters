package me.hhitt.disasters.disaster;

import org.bukkit.scheduler.BukkitRunnable;

public class DisasterTask extends BukkitRunnable {
    private int time = 0;

    @Override
    public void run() {
        if (time >= 4) {
            DisasterRegistry.pulseAll(time);
        }
        time++;
        if (time >= 100) {
            time = 3;
        }
    }
}
