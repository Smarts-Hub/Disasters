package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.disaster.DisasterRegistry;
import me.hhitt.disasters.disaster.impl.BlockDisappear;
import me.hhitt.disasters.disaster.impl.FloorIsLava;
import me.hhitt.disasters.disaster.impl.Lag;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Random;

public final class PlayerMoveListener implements Listener {

    private final ArenaManager arenaManager;
    private final Random random = new Random();

    public PlayerMoveListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        final Arena arena = arenaManager.getArena(event.getPlayer());
        if (arena == null) {
            return;
        }

        if (arena.isWaiting()) {
            return;
        }

        if (hasDisaster(arena, FloorIsLava.class)) {
            if (arena.getBorderService().isLocationInArenaTp(event.getPlayer())) {
                DisasterRegistry.addBlockToFloorIsLava(arena, event.getTo());
            }
        }

        if (hasDisaster(arena, BlockDisappear.class)) {
            if (arena.getBorderService().isLocationInArenaTp(event.getPlayer())) {
                final Location blockBelowFrom = event.getFrom().clone().subtract(0.0, 1.0, 0.0);
                DisasterRegistry.setBlockUnoccupied(arena, blockBelowFrom);

                DisasterRegistry.addBlockToDisappear(arena, event.getTo());
            }
        }

        if (!hasDisaster(arena, Lag.class)) {
            return;
        }

        if (random.nextDouble() > 0.45) {
            return;
        }
        switch (random.nextInt(5)) {
            case 0:
                event.setCancelled(true);
                break;
            case 1: {
                final Location from = event.getFrom();
                final Location to = event.getTo();
                if (to == null) {
                    return;
                }
                final double dx = from.getX() - to.getX();
                final double dz = from.getZ() - to.getZ();
                final Location back = to.clone().add(dx, 0.0, dz);
                event.setTo(back);
                break;
            }
            case 2: {
                final Location from = event.getFrom();
                final Location to = event.getTo();
                if (to == null) {
                    return;
                }
                final double dx = to.getX() - from.getX();
                final double dz = to.getZ() - from.getZ();
                final Location forward = to.clone().add(dx, 0.0, dz);
                event.setTo(forward);
                break;
            }
            case 3: {
                final Location to = event.getTo();
                if (to == null) {
                    return;
                }
                final double randX = random.nextDouble() * 4.0 - 2.0;
                final double randZ = random.nextDouble() * 4.0 - 2.0;
                final Location newLoc = to.clone().add(randX, 0.0, randZ);
                event.getPlayer().teleport(newLoc);
                break;
            }
            case 4:
                break;
        }
    }

    private boolean hasDisaster(final Arena arena, final Class<?> type) {
        for (final Object disaster : arena.getDisasters()) {
            if (type.isInstance(disaster)) {
                return true;
            }
        }
        return false;
    }
}
