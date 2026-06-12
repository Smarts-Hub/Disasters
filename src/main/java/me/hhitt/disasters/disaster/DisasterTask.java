package me.hhitt.disasters.disaster;

import org.bukkit.scheduler.BukkitRunnable;

public final class DisasterTask extends BukkitRunnable {
    @Override
    public void run() {
        DisasterRegistry.pulseAll();
    }
}
