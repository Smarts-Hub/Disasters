package me.hhitt.disasters.game.countdown;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.game.FinishReason;
import me.hhitt.disasters.game.GameSession;
import me.hhitt.disasters.game.modification.vote.GameModificationVoteManager;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Notify;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public final class Countdown extends BukkitRunnable {

    private final Arena arena;
    private final GameSession session;
    private int time = 0;
    private int remaining;
    private GameModificationVoteManager voteManager;

    public Countdown(final Arena arena, final GameSession session) {
        this.arena = arena;
        this.session = session;
        this.remaining = arena.getCountdown();
    }

    public int getTime() {
        return time;
    }

    public int getRemaining() {
        return remaining;
    }

    public void cancelVote() {
        if (voteManager != null) {
            voteManager.cancelVote();
            voteManager = null;
        }
    }

    @Override
    public void run() {
        if (time >= arena.getCountdown()) {
            if (time >= (arena.getCountdown() + 2)) {
                Notify.gameStart(arena);

                session.startGameTimer(resolveVoteNow());
                return;
            }
            time++;
            return;
        }

        if (arena.getAlive().size() <= arena.getAliveToEnd()) {
            session.finish(FinishReason.COUNTDOWN_CANCELLED);
            return;
        }

        if (time == 0 && voteManager == null) {
            final me.hhitt.disasters.storage.file.Configuration cfg = FileManager.get("config");
            if (cfg != null
                && cfg.getBoolean("game-modifications.voting.enabled", true)
                && cfg.getBoolean("game-modifications.voting.open-at-countdown-start", true)) {
                final GameModificationVoteManager manager = new GameModificationVoteManager(arena);
                manager.startVote();
                voteManager = manager;
            }
        }

        Notify.countdown(arena, remaining);
        time++;
        remaining--;
    }

    public List<String> resolveVoteNow() {
        if (voteManager != null) {
            final List<String> selected = voteManager.resolveVote();
            voteManager = null;
            return selected;
        }
        final me.hhitt.disasters.storage.file.Configuration cfg = FileManager.get("config");
        if (cfg != null && cfg.getBoolean("game-modifications.voting.enabled", true)) {
            return GameModificationVoteManager.resolveDefaultSelection(arena);
        }
        return java.util.Collections.<String>emptyList();
    }
}
