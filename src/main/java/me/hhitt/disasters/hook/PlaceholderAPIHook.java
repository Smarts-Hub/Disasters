package me.hhitt.disasters.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.game.GameState;
import me.hhitt.disasters.storage.data.Data;
import me.hhitt.disasters.storage.file.FileManager;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final ArenaManager arenaManager;

    public PlaceholderAPIHook(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public String getIdentifier() {
        return "disasters";
    }

    @Override
    public String getAuthor() {
        return "hhitt";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params.endsWith("_state") || params.endsWith("_players") || params.endsWith("_alive")
            || params.endsWith("_max") || params.endsWith("_min") || params.endsWith("_is_full")) {
            String arenaId = params.substring(0, params.lastIndexOf('_'));
            Arena arena = arenaManager.getArena(arenaId);
            if (arena == null) return "";
            if (params.endsWith("_state")) return getStateString(arena.getState());
            if (params.endsWith("_players")) return String.valueOf(arena.getPlaying().size());
            if (params.endsWith("_alive")) return String.valueOf(arena.getAlive().size());
            if (params.endsWith("_max")) return String.valueOf(arena.getMaxPlayers());
            if (params.endsWith("_min")) return String.valueOf(arena.getMinPlayers());
            if (params.endsWith("_is_full")) return String.valueOf(arena.isFull());
            return "";
        }

        if (params.equals("wins") || params.equals("player_wins")) {
            return String.valueOf(Data.getWinsFromCache(player.getUniqueId()));
        }
        if (params.equals("defeats") || params.equals("player_defeats")) {
            return String.valueOf(Data.getDefeatsFromCache(player.getUniqueId()));
        }
        if (params.equals("total_played") || params.equals("player_total_played")) {
            return String.valueOf(Data.getTotalPlayedFromCache(player.getUniqueId()));
        }
        if (params.equals("player_wlr")) {
            int wins = Data.getWinsFromCache(player.getUniqueId());
            int defeats = Data.getDefeatsFromCache(player.getUniqueId());
            if (defeats == 0) return String.valueOf(wins);
            return String.format("%.2f", (double) wins / (double) defeats);
        }

        if (params.equals("total_playing") || params.equals("global_players_total")) {
            int total = 0;
            for (Arena a : arenaManager.getArenas()) {
                total += a.getPlaying().size();
            }
            return String.valueOf(total);
        }
        if (params.equals("disasters_players_alive_total")) {
            int total = 0;
            for (Arena a : arenaManager.getArenas()) {
                total += a.getAlive().size();
            }
            return String.valueOf(total);
        }
        if (params.equals("global_arenas_count")) {
            return String.valueOf(arenaManager.getArenas().size());
        }
        if (params.equals("global_arenas_waiting")) {
            int count = 0;
            for (Arena a : arenaManager.getArenas()) {
                if (a.isWaiting()) count++;
            }
            return String.valueOf(count);
        }
        if (params.equals("global_arenas_running")) {
            int count = 0;
            for (Arena a : arenaManager.getArenas()) {
                if (a.getState() == GameState.LIVE) count++;
            }
            return String.valueOf(count);
        }

        if (params.equals("arena") || params.equals("player_arena_name")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? arena.getDisplayName() : "Not in an arena";
        }
        if (params.equals("player_arena_id")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? arena.getName() : "";
        }
        if (params.equals("arena_playing") || params.equals("game_players")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getPlaying().size()) : "0";
        }
        if (params.equals("is_in_arena") || params.equals("player_is_in_arena")) {
            return String.valueOf(arenaManager.getArena(player) != null);
        }

        if (params.equals("game_max_players")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getMaxPlayers()) : "0";
        }
        if (params.equals("game_min_players")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getMinPlayers()) : "0";
        }
        if (params.equals("game_state")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? getStateString(arena.getState()) : "";
        }
        if (params.equals("game_time")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getGameTime()) : "0";
        }
        if (params.equals("game_time_left")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getTimeLeft()) : "0";
        }
        if (params.equals("countdown_time")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getCountdownTime()) : "0";
        }
        if (params.equals("countdown_time_left")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getCountdownLeft()) : "0";
        }
        if (params.equals("game_alive")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getAlive().size()) : "0";
        }
        if (params.equals("game_spectators")) {
            Arena arena = arenaManager.getArena(player);
            if (arena != null) {
                return String.valueOf(Math.max(arena.getPlaying().size() - arena.getAlive().size(), 0));
            }
            return "0";
        }
        if (params.equals("game_disasters_count")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.getDisasters().size()) : "0";
        }
        if (params.equals("game_disasters_list")) {
            Arena arena = arenaManager.getArena(player);
            if (arena == null) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arena.getDisasters().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(arena.getDisasters().get(i).getClass().getSimpleName());
            }
            return sb.toString();
        }
        if (params.equals("game_is_full")) {
            Arena arena = arenaManager.getArena(player);
            return arena != null ? String.valueOf(arena.isFull()) : "false";
        }
        if (params.equals("player_is_alive")) {
            Arena arena = arenaManager.getArena(player);
            if (arena != null) {
                return String.valueOf(arena.getAlive().contains(player));
            }
            return "false";
        }

        return "Invalid placeholder";
    }

    private String getStateString(GameState state) {
        org.bukkit.configuration.ConfigurationSection lang = FileManager.get("lang");
        if (lang == null) return state.name();
        switch (state) {
            case RECRUITING: {
                String s = lang.getString("game-state-placeholders.recruiting");
                return s != null ? s : state.name();
            }
            case COUNTDOWN: {
                String s = lang.getString("game-state-placeholders.countdown");
                return s != null ? s : state.name();
            }
            case LIVE: {
                String s = lang.getString("game-state-placeholders.live");
                return s != null ? s : state.name();
            }
            case RESTARTING: {
                String s = lang.getString("game-state-placeholders.restarting");
                return s != null ? s : state.name();
            }
            default:
                return state.name();
        }
    }
}
