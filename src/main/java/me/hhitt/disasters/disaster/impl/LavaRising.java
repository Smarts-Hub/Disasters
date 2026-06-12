package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.rising.RisingFluidController;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class LavaRising implements Disaster {

    private Arena arena;
    private RisingFluidController controller;

    @Override
    public void start(final Arena arena) {
        final ConfigurationSection config = FileManager.get("config");
        final int riseInterval = config != null ? config.getInt("disasters.per-disaster.lava-rising.rise-interval-seconds", 5) : 5;
        final int maxBlocks = config != null ? config.getInt("disasters.per-disaster.lava-rising.max-blocks-per-pulse", 2500) : 2500;
        this.arena = arena;
        controller = new RisingFluidController(arena, Material.LAVA, arena.getDisasterSettings().getLavaRisingStartY(), riseInterval, maxBlocks);
        Notify.disaster(arena, "lava-rising");
    }

    @Override
    public void pulse(final int time) {
        if (controller == null || arena == null) {
            return;
        }
        controller.pulse(time);
        if (time % controller.getRiseIntervalSeconds() == 0) {
            for (Player player : new ArrayList<>(arena.getAlive())) {
                final Material feet = player.getLocation().getBlock().getType();
                final Material head = player.getLocation().clone().add(0.0, 1.0, 0.0).getBlock().getType();
                if (feet == Material.LAVA || head == Material.LAVA) {
                    DeathMessageService.mark(player, "lava-rising");
                    player.setFireTicks(Math.max(player.getFireTicks(), 80));
                    player.damage(3.0);
                }
            }
        }
    }

    @Override
    public void stop(final Arena arena) {
        this.arena = null;
        controller = null;
    }
}
