package me.hhitt.disasters.disaster

import org.bukkit.scheduler.BukkitRunnable

/**
 * DisasterTask is a BukkitRunnable that pulses all disasters every second.
 **/

class DisasterTask : BukkitRunnable() {
    private var time = 0
    override fun run() {
        if(time >= 4) {
            DisasterRegistry.pulseAll(time)
        }
        time++
        if(time >= 100) {
            time = 3
        }
    }
}
