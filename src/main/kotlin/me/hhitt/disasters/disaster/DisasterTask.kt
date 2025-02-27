package me.hhitt.disasters.disaster

import org.bukkit.scheduler.BukkitRunnable

class DisasterTask : BukkitRunnable() {
    override fun run() {
        DisasterRegistry.pulseAll()
    }
}
