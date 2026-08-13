package me.hhitt.disasters.command;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.game.ForceStartResult;
import me.hhitt.disasters.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ArenaCommand implements TabExecutor {

    private final ArenaManager arenaManager;

    public ArenaCommand(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            return false;
        }

        final String route = args[0].toLowerCase(Locale.ROOT);
        if ("join".equals(route) && args.length == 2) {
            if (!(sender instanceof Player)) return true;
            final Player player = (Player) sender;
            final Arena arena = resolveArena(sender, args[1]);
            if (arena == null) return true;

            if (arenaManager.getArena(player) != null) {
                Msg.send(player, "already-in-arena");
                return true;
            }

            if (!arena.addPlayer(player)) {
                if (arena.isFull()) {
                    Msg.send(player, "arena-full");
                } else {
                    Msg.send(player, "arena-in-game");
                }
            }
            return true;
        }

        if ("quickjoin".equals(route) && args.length == 1) {
            if (!(sender instanceof Player)) return true;
            final Player player = (Player) sender;

            if (arenaManager.getArena(player) != null) {
                Msg.send(player, "already-in-arena");
                return true;
            }

            if (!arenaManager.addPlayerToBestArena(player)) {
                Msg.send(player, "no-available-arena");
            }
            return true;
        }

        if ("leave".equals(route) && args.length == 1) {
            if (!(sender instanceof Player)) return true;
            final Player player = (Player) sender;

            final Arena arena = arenaManager.getArena(player);
            if (arena != null) {
                arena.removePlayer(player);
            } else {
                Msg.send(player, "not-in-arena");
            }
            return true;
        }

        if ("forcestart".equals(route) && args.length == 1) {
            if (!(sender instanceof Player)) return true;
            final Player player = (Player) sender;

            final Arena arena = arenaManager.getArena(player);
            if (arena != null) {
                if (!player.hasPermission("disasters.forcestart")) {
                    Msg.send(player, "no-permission");
                    return true;
                }
                sendForceStartResult(player, arena, arena.forceStart());
            } else {
                Msg.send(player, "not-in-arena");
            }
            return true;
        }

        if ("forcestop".equals(route) && args.length == 1) {
            if (!(sender instanceof Player)) return true;
            final Player player = (Player) sender;

            final Arena arena = arenaManager.getArena(player);
            if (arena != null) {
                if (!player.hasPermission("disasters.forcestop")) {
                    Msg.send(player, "no-permission");
                    return true;
                }
                arena.stop();
            } else {
                Msg.send(player, "not-in-arena");
            }
            return true;
        }

        if ("forcestart".equals(route) && args.length == 2) {
            final Arena arena = resolveArena(sender, args[1]);
            if (arena == null) return true;

            if (!sender.hasPermission("disasters.forcestart")) {
                Msg.send(sender, "no-permission");
                return true;
            }
            sendForceStartResult(sender, arena, arena.forceStart());
            return true;
        }

        if ("forcestop".equals(route) && args.length == 2) {
            final Arena arena = resolveArena(sender, args[1]);
            if (arena == null) return true;

            if (!sender.hasPermission("disasters.forcestop")) {
                Msg.send(sender, "no-permission");
                return true;
            }
            arena.stop();
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 0) {
            return filterSuggestions(
                new ArrayList<String>(java.util.Arrays.asList("join", "quickjoin", "leave", "forcestart", "forcestop")),
                ""
            );
        }
        if (args.length == 1) {
            return filterSuggestions(
                new ArrayList<String>(java.util.Arrays.asList("join", "quickjoin", "leave", "forcestart", "forcestop")),
                args[0]
            );
        }
        if (args.length == 2 && ("join".equalsIgnoreCase(args[0])
            || "forcestart".equalsIgnoreCase(args[0])
            || "forcestop".equalsIgnoreCase(args[0]))) {
            return filterSuggestions(arenaNames(), args[1]);
        }
        return Collections.emptyList();
    }

    private Arena resolveArena(final CommandSender sender, final String arenaId) {
        final Arena arena = arenaManager.getArena(arenaId);
        if (arena == null) {
            sendCommandError(sender, arenaId);
        }
        return arena;
    }

    private void sendCommandError(final CommandSender sender, final String arenaId) {
        sender.sendMessage(Component.text("Unknown arena: " + arenaId, NamedTextColor.RED));
    }

    private List<String> arenaNames() {
        final List<String> names = new ArrayList<>();
        for (final Arena arena : arenaManager.getArenas()) {
            names.add(arena.getName());
        }
        return names;
    }

    private List<String> filterSuggestions(final List<String> suggestions, final String prefix) {
        final String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        final List<String> filtered = new ArrayList<>();
        for (final String suggestion : suggestions) {
            if (suggestion.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
                filtered.add(suggestion);
            }
        }
        filtered.sort(String.CASE_INSENSITIVE_ORDER);
        return filtered;
    }

    private void sendForceStartResult(final CommandSender sender, final Arena arena, final ForceStartResult result) {
        final String path;
        switch (result) {
            case STARTED:
                path = "force-start-success";
                break;
            case EMPTY:
                path = "force-start-empty";
                break;
            case ALREADY_LIVE:
                path = "force-start-already-live";
                break;
            case RESTARTING:
                path = "force-start-restarting";
                break;
            default:
                path = "force-start-restarting";
                break;
        }

        final Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("%arena%", arena.getName());
        Msg.send(sender, path, replacements);
    }
}
