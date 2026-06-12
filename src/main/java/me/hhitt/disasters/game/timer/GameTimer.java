package me.hhitt.disasters.game.timer;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.DisasterRegistry;
import me.hhitt.disasters.disaster.impl.BlockDisappear;
import me.hhitt.disasters.disaster.impl.FloorIsLava;
import me.hhitt.disasters.game.FinishReason;
import me.hhitt.disasters.game.GameSession;
import me.hhitt.disasters.game.drop.ItemDropManager;
import me.hhitt.disasters.game.modification.GameModificationRegistry;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class GameTimer extends BukkitRunnable {

    private final Arena arena;
    private final GameSession session;
    private int time = 0;

    public GameTimer(final Arena arena, final GameSession session) {
        this.arena = arena;
        this.session = session;
    }

    public int getTime() {
        return time;
    }

    public int getRemaining() {
        return arena.getMaxTime() - time;
    }

    @Override
    public void run() {
        if (time >= arena.getMaxTime()) {
            session.finish(FinishReason.TIME_LIMIT);
            return;
        }

        if (arena.getAlive().size() <= arena.getAliveToEnd()) {
            session.finish(FinishReason.ALIVE_THRESHOLD);
            return;
        }

        if (time % Math.max(arena.getRate(), 1) == 0) {
            DisasterRegistry.addRandomDisaster(arena);
        }

        boolean hasFloorIsLava = false;
        for (Disaster disaster : arena.getDisasters()) {
            if (disaster instanceof FloorIsLava) {
                hasFloorIsLava = true;
                break;
            }
        }
        if (hasFloorIsLava) {
            for (Player player : arena.getAlive()) {
                DisasterRegistry.addBlockToFloorIsLava(arena, player.getLocation());
            }
        }

        boolean hasBlockDisappear = false;
        for (Disaster disaster : arena.getDisasters()) {
            if (disaster instanceof BlockDisappear) {
                hasBlockDisappear = true;
                break;
            }
        }
        if (hasBlockDisappear) {
            for (Player player : arena.getAlive()) {
                DisasterRegistry.addBlockToDisappear(arena, player.getLocation());
            }
        }

        GameModificationRegistry.pulse(arena, time);
        ItemDropManager.pulse(arena, time);

        time++;
    }
}
