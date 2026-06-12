package me.hhitt.disasters.util;

import me.clip.placeholderapi.PlaceholderAPI;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class Msg {

    private static final MiniMessage MINI_MSG = MiniMessage.miniMessage();

    private Msg() {
    }

    public static Component parse(final String msg, final Player player) {
        String text = msg != null ? msg : "";
        return MINI_MSG.deserialize(placeholder(text, player));
    }

    public static Component parse(final String msg) {
        String text = msg != null ? msg : "";
        return MINI_MSG.deserialize(text);
    }

    public static List<Component> parseList(final List<String> lore, final Player player) {
        final List<Component> components = new ArrayList<>();
        for (final String line : lore) {
            components.add(parse(line, player));
        }
        return components;
    }

    public static void send(final Player player, final String path) {
        player.sendMessage(parse(getMsg(path), player));
    }

    public static void send(final CommandSender sender, final String path) {
        sender.sendMessage(parse(getMsg(path)));
    }

    public static void sendParsed(final Player player, final String msg) {
        player.sendMessage(parse(msg, player));
    }

    public static void sendTitle(final Player player, final String title) {
        player.sendTitlePart(TitlePart.TITLE, parse(title, player));
    }

    public static void sendSubtitle(final Player player, final String subtitle) {
        player.sendTitlePart(TitlePart.SUBTITLE, parse(subtitle, player));
    }

    public static String placeholder(final String msg, final Player player) {
        return PlaceholderAPI.setPlaceholders(player, msg);
    }

    public static void sendActionbar(final Player player, final String bar) {
        player.sendActionBar(parse(bar, player));
    }

    public static void playSound(final Player player, final String sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    private static String getMsg(final String path) {
        final Configuration lang = FileManager.get("lang");
        if (lang == null) {
            return "Message not found";
        }
        final String message = lang.getString("messages." + path);
        return message != null ? message : "Message not found";
    }
}
