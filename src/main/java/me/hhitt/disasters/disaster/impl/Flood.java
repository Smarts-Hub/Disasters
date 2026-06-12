package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.rising.RisingFluidController;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Material;

public class Flood implements Disaster {

    private RisingFluidController controller;

    @Override
    public void start(final Arena arena) {
        final Configuration config = FileManager.get("config");
        final int riseInterval = config != null ? config.getInt("disasters.per-disaster.flood.rise-interval-seconds", 4) : 4;
        final int maxBlocks = config != null ? config.getInt("disasters.per-disaster.flood.max-blocks-per-pulse", 2500) : 2500;
        controller = new RisingFluidController(arena, Material.WATER, arena.getDisasterSettings().getFloodStartY(), riseInterval, maxBlocks);
        Notify.disaster(arena, "flood");
    }

    @Override
    public void pulse(final int time) {
        if (controller != null) {
            controller.pulse(time);
        }
    }

    @Override
    public void stop(final Arena arena) {
        controller = null;
    }
}
