package me.hhitt.disasters.arena;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.hhitt.disasters.arena.service.BorderService;
import me.hhitt.disasters.arena.service.ResetArenaService;
import me.hhitt.disasters.arena.service.RespawnService;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.impl.WorldBorder;
import me.hhitt.disasters.game.FinishReason;
import me.hhitt.disasters.game.ForceStartResult;
import me.hhitt.disasters.game.GameSession;
import me.hhitt.disasters.game.GameState;
import me.hhitt.disasters.game.modification.GameModification;
import me.hhitt.disasters.model.arena.JumpPad;
import me.hhitt.disasters.util.Lobby;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class Arena {

    private final String name;
    private final String displayName;
    private final int minPlayers;
    private final int maxPlayers;
    private final int aliveToEnd;
    private final int maxTime;
    private final int countdown;
    private final int rate;
    private final int maxDisasters;
    private final Location location;
    private final List<Location> spawns;
    private final List<JumpPad> jumpPads;
    private final Location corner1;
    private final Location corner2;
    private final List<String> winnersCommands;
    private final List<String> losersCommands;
    private final List<String> toAllCommands;
    private final ArenaDisasterSettings disasterSettings;

    private final List<Player> playing = new ArrayList<>();
    private final List<Player> alive = new ArrayList<>();
    private final List<Disaster> disasters = new ArrayList<>();
    private final List<GameModification> activeGameModifications = new ArrayList<>();
    private GameState state = GameState.RECRUITING;

    private final BorderService borderService;
    private final ResetArenaService resetService;
    private final RespawnService respawnService;
    private final GameSession gameSession;

    public Arena(
        final String name,
        final String displayName,
        final int minPlayers,
        final int maxPlayers,
        final int aliveToEnd,
        final int maxTime,
        final int countdown,
        final int rate,
        final int maxDisasters,
        final Location location,
        final List<Location> spawns,
        final List<JumpPad> jumpPads,
        final Location corner1,
        final Location corner2,
        final List<String> winnersCommands,
        final List<String> losersCommands,
        final List<String> toAllCommands,
        final ArenaDisasterSettings disasterSettings,
        final WorldEditPlugin worldEdit
    ) {
        this.name = name;
        this.displayName = displayName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.aliveToEnd = aliveToEnd;
        this.maxTime = maxTime;
        this.countdown = countdown;
        this.rate = rate;
        this.maxDisasters = maxDisasters;
        this.location = location;
        this.spawns = spawns != null ? spawns : Collections.<Location>emptyList();
        this.jumpPads = jumpPads != null ? jumpPads : Collections.<JumpPad>emptyList();
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.winnersCommands = winnersCommands;
        this.losersCommands = losersCommands;
        this.toAllCommands = toAllCommands;
        this.disasterSettings = Objects.requireNonNull(disasterSettings, "disasterSettings");
        this.borderService = new BorderService(corner1, corner2);
        this.resetService = new ResetArenaService(this, worldEdit);
        this.respawnService = new RespawnService(this);
        this.gameSession = new GameSession(this);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getAliveToEnd() {
        return aliveToEnd;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getCountdown() {
        return countdown;
    }

    public int getRate() {
        return rate;
    }

    public int getMaxDisasters() {
        return maxDisasters;
    }

    public Location getLocation() {
        return location;
    }

    public List<Location> getSpawns() {
        return spawns;
    }

    public List<JumpPad> getJumpPads() {
        return jumpPads;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public List<String> getWinnersCommands() {
        return winnersCommands;
    }

    public List<String> getLosersCommands() {
        return losersCommands;
    }

    public List<String> getToAllCommands() {
        return toAllCommands;
    }

    public ArenaDisasterSettings getDisasterSettings() {
        return disasterSettings;
    }

    public List<Player> getPlaying() {
        return playing;
    }

    public List<Player> getAlive() {
        return alive;
    }

    public List<Disaster> getDisasters() {
        return disasters;
    }

    public List<GameModification> getActiveGameModifications() {
        return activeGameModifications;
    }

    public GameState getState() {
        return state;
    }

    public void setState(final GameState state) {
        this.state = state;
    }

    public BorderService getBorderService() {
        return borderService;
    }

    public ResetArenaService getResetService() {
        return resetService;
    }

    public RespawnService getRespawnService() {
        return respawnService;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public boolean addPlayer(final Player player) {
        if (playing.contains(player)) {
            return false;
        }
        if (!isWaiting()) {
            return false;
        }
        if (playing.size() >= maxPlayers) {
            return false;
        }
        Lobby.savePlayerState(player);
        player.getInventory().clear();
        playing.add(player);
        alive.add(player);
        final Location spawn = !spawns.isEmpty()
            ? spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()))
            : location;
        player.teleport(spawn);
        Notify.playerJoined(player, this);
        if (playing.size() == minPlayers) {
            start();
        }
        return true;
    }

    public void playerDied(final Player player) {
        alive.remove(player);
        switch (state) {
            case LIVE:
                respawnService.setSpectator(player);
                break;
            default:
                respawnService.respawnAtArena(player);
                break;
        }
    }

    public void removePlayer(final Player player) {
        for (final Disaster disaster : disasters) {
            if (disaster instanceof WorldBorder) {
                resetWorldBorder(player);
                break;
            }
        }
        Lobby.teleportPlayer(player);
        playing.remove(player);
        alive.remove(player);
        if (state == GameState.COUNTDOWN) {
            gameSession.cancelCountdownIfBlocked();
        } else if (state == GameState.RECRUITING && playing.size() < minPlayers) {
            stop();
        } else {
            if (alive.size() < aliveToEnd) {
                stop();
            }
        }
        Notify.playerLeft(player, this);
    }

    public boolean isFull() {
        return playing.size() >= maxPlayers;
    }

    public boolean isEmpty() {
        return playing.size() == 0;
    }

    public boolean isWaiting() {
        return state == GameState.RECRUITING || state == GameState.COUNTDOWN;
    }

    public boolean isPlayerValid(final Player player) {
        return playing.contains(player);
    }

    public void start() {
        gameSession.start();
    }

    public ForceStartResult forceStart() {
        return gameSession.forceStartNow();
    }

    public void stop() {
        gameSession.finish(FinishReason.ADMIN_STOP);
    }

    public void clear() {
        playing.clear();
        alive.clear();
        disasters.clear();
        activeGameModifications.clear();
    }

    public int getTimeLeft() {
        return gameSession.getTimeLeft();
    }

    public int getGameTime() {
        return gameSession.getGameTime();
    }

    public int getCountdownTime() {
        return gameSession.getCountdownTime();
    }

    public int getCountdownLeft() {
        return gameSession.getCountdownLeft();
    }

    private void resetWorldBorder(final Player player) {
        player.setWorldBorder(null);
    }
}
