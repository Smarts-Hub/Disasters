package me.hhitt.disasters.command

import me.hhitt.disasters.obj.entity.DisasterSheep
import org.bukkit.scheduler.BukkitRunnable

class SheepRun(private val sheep: DisasterSheep): BukkitRunnable() {

    var ticks = 4

    override fun run() {
        sheep.call()
        if(ticks == 0){
            cancel()
        }
        ticks--
    }

}