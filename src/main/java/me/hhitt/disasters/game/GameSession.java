package me.hhitt.disasters.game;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.DisasterRegistry;
import me.hhitt.disasters.game.countdown.Countdown;
import me.hhitt.disasters.game.drop.ItemDropManager;
import me.hhitt.disasters.game.modification.GameModificationRegistry;
import me.hhitt.disasters.game.modification.vote.GameModificationVoteManager;
import me.hhitt.disasters.game.timer.GameTimer;
import me.hhitt.disasters.storage.data.Data;
import me.hhitt.disasters.util.Lobby;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class GameSession {

    private final Arena arena;
    private final Disasters plugin;
    private BukkitTask countdownTask;
    private BukkitTask timerTask;
    private Countdown countdown;
    private GameTimer gameTimer;
    private final Set<UUID> participantIds = new HashSet<UUID>();
    private boolean finishing;

    public GameSession(final Arena arena) {
        this.arena = arena;
        this.plugin = Disasters.getInstance();
    }

    public void start() {
        if (arena.getState() != GameState.RECRUITING) {
            return;
        }
        arena.setState(GameState.COUNTDOWN);
        startCountdown();
    }

    private void startCountdown() {
        arena.getResetService().save();
        final Countdown cd = new Countdown(arena, this);
        countdown = cd;
        countdownTask = cd.runTaskTimer(plugin, 0, 20L);
    }

    public ForceStartResult forceStartNow() {
        if (arena.isEmpty()) {
            return ForceStartResult.EMPTY;
        }

        final GameState state = arena.getState();
        final List<String> selectedModificationIds;
        switch (state) {
            case LIVE:
                return ForceStartResult.ALREADY_LIVE;
            case RESTARTING:
                return ForceStartResult.RESTARTING;
            case RECRUITING:
                arena.setState(GameState.COUNTDOWN);
                try {
                    arena.getResetService().save();
                } catch (final RuntimeException ex) {
                    arena.setState(GameState.RECRUITING);
                    throw ex;
                }
                selectedModificationIds = GameModificationVoteManager.resolveDefaultSelection(arena);
                break;
            case COUNTDOWN:
                selectedModificationIds = countdown != null
                    ? countdown.resolveVoteNow()
                    : GameModificationVoteManager.resolveDefaultSelection(arena);
                break;
            default:
                return ForceStartResult.ALREADY_LIVE;
        }

        Notify.gameStart(arena);
        startGameTimer(selectedModificationIds);
        return ForceStartResult.STARTED;
    }

    public void startGameTimer(final List<String> selectedModificationIds) {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (countdown != null) {
            countdown.cancelVote();
        }
        countdown = null;
        final List<String> selectedIds = selectedModificationIds != null
            ? selectedModificationIds
            : Collections.<String>emptyList();
        participantIds.clear();
        for (final Player player : arena.getPlaying()) {
            participantIds.add(player.getUniqueId());
        }
        arena.setState(GameState.LIVE);
        ItemDropManager.cleanOrphanedEntities(arena);
        GameModificationRegistry.start(arena, selectedIds);
        final GameTimer timer = new GameTimer(arena, this);
        gameTimer = timer;
        timerTask = timer.runTaskTimer(plugin, 0, 20L);
    }

    public void finish(final FinishReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("FinishReason must not be null");
        }
        if (finishing) {
            return;
        }
        try {
            finishing = true;
            final GameState previousState = arena.getState();

            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
            if (countdown != null) {
                countdown.cancelVote();
            }
            countdown = null;
            gameTimer = null;

            if (previousState == GameState.COUNTDOWN && reason != FinishReason.PLUGIN_DISABLE) {
                Notify.countdownCanceled(arena);
                ItemDropManager.clearDrops(arena);
                GameModificationRegistry.stop(arena);
                DisasterRegistry.removeDisasters(arena);
            } else if (previousState == GameState.LIVE) {
                arena.setState(GameState.RESTARTING);

                final Set<UUID> winners = new HashSet<UUID>();
                for (final Player player : arena.getAlive()) {
                    if (participantIds.contains(player.getUniqueId())) {
                        winners.add(player.getUniqueId());
                    }
                }
                final Set<UUID> losers = new HashSet<UUID>(participantIds);
                losers.removeAll(winners);

                if (reason.shouldRecordOutcome()) {
                    if (!winners.isEmpty() || !losers.isEmpty()) {
                        Data.recordMatchResults(winners, losers).exceptionally(throwable -> {
                            Disasters.getInstance().getLogger().log(Level.SEVERE,
                                "Failed to record match results for arena " + arena.getName(), throwable);
                            return null;
                        });
                    }
                }

                for (final Player player : arena.getPlaying()) {
                    final UUID uuid = player.getUniqueId();
                    final boolean isWinner = winners.contains(uuid);
                    final boolean isLoser = losers.contains(uuid);
                    final List<String> commands;
                    if (isWinner) {
                        commands = arena.getWinnersCommands();
                    } else if (isLoser) {
                        commands = arena.getLosersCommands();
                    } else {
                        commands = Collections.emptyList();
                    }
                    for (final String command : commands) {
                        dispatchCommand(player, command);
                    }
                    for (final String command : arena.getToAllCommands()) {
                        dispatchCommand(player, command);
                    }
                }

                if (reason != FinishReason.PLUGIN_DISABLE) {
                    Notify.gameEnd(arena);
                }

                GameModificationRegistry.stop(arena);
                DisasterRegistry.removeDisasters(arena);
                ItemDropManager.clearDrops(arena);

                if (reason != FinishReason.PLUGIN_DISABLE) {
                    Lobby.teleportAtEnd(arena);
                    arena.getResetService().paste();
                } else {
                    for (final Player player : arena.getPlaying()) {
                        Lobby.teleportPlayer(player);
                    }
                    arena.clear();
                }
            } else {
                ItemDropManager.clearDrops(arena);
                GameModificationRegistry.stop(arena);
                DisasterRegistry.removeDisasters(arena);
            }

            participantIds.clear();
            if (reason != FinishReason.PLUGIN_DISABLE) {
                arena.setState(GameState.RECRUITING);
            }
        } finally {
            finishing = false;
        }
    }

    private void dispatchCommand(final Player player, final String command) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            final String parsed = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public int getTimeLeft() {
        if (gameTimer != null) {
            return gameTimer.getRemaining();
        }
        if (countdown != null) {
            return countdown.getRemaining();
        }
        return 0;
    }

    public int getGameTime() {
        if (gameTimer != null) {
            return gameTimer.getTime();
        }
        return 0;
    }

    public int getCountdownTime() {
        if (countdown != null) {
            return countdown.getTime();
        }
        return 0;
    }

    public int getCountdownLeft() {
        if (countdown != null) {
            return countdown.getRemaining();
        }
        return 0;
    }
}
