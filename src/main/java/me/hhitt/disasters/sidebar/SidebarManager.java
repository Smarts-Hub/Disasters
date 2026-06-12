package me.hhitt.disasters.sidebar;

import fr.mrmicky.fastboard.adventure.FastBoard;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.DisasterRegistry;
import me.hhitt.disasters.game.GameState;
import me.hhitt.disasters.game.modification.GameModificationRegistry;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SidebarManager {

    private final Configuration config;
    private final Configuration mainConfig;
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final Map<UUID, GameState> states = new HashMap<>();

    public SidebarManager() {
        this.config = FileManager.get("scoreboard");
        this.mainConfig = FileManager.get("config");
    }

    public void updateSidebar(final Player player, final GameState state, final Arena arena) {
        final UUID playerId = player.getUniqueId();

        FastBoard board = boards.get(playerId);
        if (board == null) {
            board = new FastBoard(player);
            boards.put(playerId, board);
        }

        final boolean isNewPlayer = !states.containsKey(playerId);
        final GameState currentState = states.get(playerId);
        final boolean stateChanged = currentState != state;
        final boolean isDynamic = state == GameState.COUNTDOWN || state == GameState.LIVE;

        if (isNewPlayer || stateChanged || isDynamic) {
            updateBoardContent(board, state, player, arena);
            states.put(playerId, state);
        }
    }

    private void updateBoardContent(final FastBoard board, final GameState state, final Player player, final Arena arena) {
        if (state == null) {
            board.updateTitle(Msg.parse(config.getString("lobby.title"), player));
            final List<String> lines = config.getStringList("lobby.lines");
            board.updateLines(parseLines(lines, player));
            return;
        }

        switch (state) {
            case RECRUITING:
                board.updateTitle(Msg.parse(config.getString("recruiting.title"), player));
                final List<String> recruitingLines = config.getStringList("recruiting.lines");
                board.updateLines(parseLines(recruitingLines, player));
                break;
            case COUNTDOWN:
                board.updateTitle(Msg.parse(config.getString("countdown.title"), player));
                final List<String> countdownLines = config.getStringList("countdown.lines");
                board.updateLines(parseLines(countdownLines, player));
                break;
            case LIVE:
                board.updateTitle(Msg.parse(config.getString("live.title"), player));
                final List<String> liveLines = config.getStringList("live.lines");
                final List<Component> parsed = new ArrayList<>(parseLines(liveLines, player));

                if (arena != null && mainConfig.getBoolean("show-active-disasters", true)) {
                    final List<String> disasters = DisasterRegistry.getActiveDisasterNames(arena);
                    final List<String> modifications = GameModificationRegistry.displayNames(arena);
                    if (!disasters.isEmpty() || !modifications.isEmpty()) {
                        parsed.add(Component.empty());
                        parsed.add(Msg.parse("<gray>Active:", player));
                        for (final String name : disasters) {
                            parsed.add(Msg.parse("<red>  " + name, player));
                        }
                        for (final String name : modifications) {
                            parsed.add(Msg.parse("<aqua>  " + name, player));
                        }
                    }
                }

                board.updateLines(parsed);
                break;
            case RESTARTING:
                board.updateTitle(Msg.parse(config.getString("restarting.title"), player));
                final List<String> restartingLines = config.getStringList("restarting.lines");
                board.updateLines(parseLines(restartingLines, player));
                break;
            default:
                board.updateTitle(Msg.parse(config.getString("lobby.title"), player));
                final List<String> defaultLines = config.getStringList("lobby.lines");
                board.updateLines(parseLines(defaultLines, player));
                break;
        }
    }

    private List<Component> parseLines(final List<String> lines, final Player player) {
        final List<Component> components = new ArrayList<>();
        for (final String line : lines) {
            components.add(Msg.parse(line, player));
        }
        return components;
    }

    public void cleanupOfflinePlayers() {
        boards.entrySet().removeIf(entry -> {
            final UUID uuid = entry.getKey();
            final FastBoard board = entry.getValue();
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                board.delete();
                states.remove(uuid);
                return true;
            }
            return false;
        });
    }

    public void removeBoard(final Player player) {
        final FastBoard board = boards.remove(player.getUniqueId());
        states.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public void shutdown() {
        for (final FastBoard board : boards.values()) {
            board.delete();
        }
        boards.clear();
        states.clear();
    }

    public void updateAllBoards() {
        boards.entrySet().removeIf(entry -> {
            final UUID uuid = entry.getKey();
            final FastBoard board = entry.getValue();
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                board.delete();
                states.remove(uuid);
                return true;
            }
            return false;
        });
    }
}
