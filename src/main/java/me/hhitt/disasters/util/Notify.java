package me.hhitt.disasters.util;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Notify {

    private Notify() {
    }

    public static void countdown(final Arena arena, final int index) {
        final Configuration config = config();
        final String path = "countdown." + index;
        final String title = config.getString(path + ".title", "");
        final String subtitle = config.getString(path + ".subtitle", "");
        sendTitleToArena(arena, title, subtitle);
        sound(arena, Sound.ENTITY_PLAYER_LEVELUP);
    }

    public static void countdownCanceledNotEnoughPlayers(final Arena arena) {
        final Map<String, String> replacements = new HashMap<>();
        replacements.put("%current_players%", Integer.toString(arena.getPlaying().size()));
        replacements.put("%min_players%", Integer.toString(arena.getMinPlayers()));
        countdownCanceled(arena, "countdown-canceled.not-enough-players", replacements);
    }

    public static void countdownCanceledEndThreshold(final Arena arena) {
        final Map<String, String> replacements = new HashMap<>();
        replacements.put("%current_players%", Integer.toString(arena.getPlaying().size()));
        replacements.put("%alive_to_end%", Integer.toString(arena.getAliveToEnd()));
        replacements.put("%required_players%", Integer.toString(arena.getAliveToEnd() + 1));
        countdownCanceled(arena, "countdown-canceled.end-threshold", replacements);
    }

    public static void countdownCanceledByAdmin(final Arena arena) {
        countdownCanceled(arena, "countdown-canceled.admin-stop", new HashMap<String, String>());
    }

    private static void countdownCanceled(final Arena arena, final String path, final Map<String, String> replacements) {
        final Configuration config = config();
        String title = config.getString(path + ".title", "");
        String subtitle = config.getString(path + ".subtitle", "");
        for (final Map.Entry<String, String> entry : replacements.entrySet()) {
            title = title.replace(entry.getKey(), entry.getValue());
            subtitle = subtitle.replace(entry.getKey(), entry.getValue());
        }
        sendTitleToArena(arena, title, subtitle);
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BASS);
    }

    public static void gameStart(final Arena arena) {
        final Configuration config = config();
        final String title = config.getString("game-start.title", "");
        final String subtitle = config.getString("game-start.subtitle", "");
        sendTitleToArena(arena, title, subtitle);
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BELL);
    }

    public static void gameEnd(final Arena arena) {
        final Configuration config = config();
        final String title = config.getString("game-end.title", "");
        final String subtitle = config.getString("game-end.subtitle", "");
        sendTitleToArena(arena, title, subtitle);
        sound(arena, Sound.BLOCK_NOTE_BLOCK_BASS);
    }

    public static void disaster(final Arena arena, final String disaster) {
        final Configuration config = config();
        final String path = "disaster." + disaster;
        final String title = config.getString(path + ".title", "");
        final String subtitle = config.getString(path + ".subtitle", "");
        final List<String> chatMessages = config.getStringList(path + ".chat");

        sendTitleToArena(arena, title, subtitle);
        sendChatMessagesToArena(arena, chatMessages);
        sound(arena, Sound.ENTITY_ENDER_DRAGON_SHOOT);
    }

    public static void playerJoined(final Player player, final Arena arena) {
        final Configuration config = config();
        final String message = config.getString("game-broadcast.player-joined", "");
        final String msg = message.replace("%joined%", player.getName());
        sendChatMessageToArena(arena, msg);
        sound(arena, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }

    public static void playerLeft(final Player player, final Arena arena) {
        final Configuration config = config();
        final String message = config.getString("game-broadcast.player-left", "");
        final String msg = message.replace("%left%", player.getName());
        sendChatMessageToArena(arena, msg);
        sound(arena, Sound.ENTITY_ALLAY_DEATH);
    }

    public static void playerKilled(final Player player, final Player killer, final Arena arena) {
        final Configuration config = config();
        final String message = config.getString("game-broadcast.player-killed", "");
        final String msg = message.replace("%killer%", player.getName());
        sendChatMessageToArena(arena, msg);
    }

    public static void playerDied(final Player player, final Arena arena) {
        final Configuration config = config();
        final String message = config.getString("game-broadcast.player-died", "");
        final String msg = message.replace("%dead%", player.getName());
        sendChatMessageToArena(arena, msg);
    }

    public static void playerWon(final Player player, final Arena arena) {
        final Configuration config = config();
        final String message = config.getString("game-broadcast.player-won", "");
        final String msg = message.replace("%winner%", player.getName());
        sendChatMessageToArena(arena, msg);
        sound(arena, Sound.ENTITY_ENDER_DRAGON_DEATH);
    }

    public static void playerMessageRaw(final Player player, final String message) {
        player.sendTitlePart(TitlePart.TITLE, Msg.parse(message, player));
    }

    private static void sendTitleToArena(final Arena arena, final String title, final String subtitle) {
        for (final Player player : arena.getPlaying()) {
            Msg.sendTitle(player, title);
            Msg.sendSubtitle(player, subtitle);
        }
    }

    private static void sendChatMessagesToArena(final Arena arena, final List<String> messages) {
        for (final Player player : arena.getPlaying()) {
            for (final String message : messages) {
                Msg.sendParsed(player, message);
            }
        }
    }

    private static void sendChatMessageToArena(final Arena arena, final String message) {
        for (final Player player : arena.getPlaying()) {
            Msg.sendParsed(player, message);
        }
    }

    private static void sound(final Arena arena, final Sound sound) {
        for (final Player player : arena.getPlaying()) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    private static Configuration config() {
        return Objects.requireNonNull(FileManager.get("lang"), "lang config missing");
    }
}
