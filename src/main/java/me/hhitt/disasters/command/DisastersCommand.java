package me.hhitt.disasters.command;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.disaster.DisasterDefinition;
import me.hhitt.disasters.disaster.DisasterRegistry;
import me.hhitt.disasters.game.modification.GameModificationDefinition;
import me.hhitt.disasters.game.modification.GameModificationRegistry;
import me.hhitt.disasters.game.drop.ItemDropManager;
import me.hhitt.disasters.service.DefinitionToggleService;
import me.hhitt.disasters.sidebar.SidebarService;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Lobby;
import me.hhitt.disasters.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DisastersCommand implements TabExecutor {

    private final ArenaManager arenaManager;
    private final SidebarService sidebarService;
    private final DefinitionToggleService definitionToggleService;

    public DisastersCommand(final ArenaManager arenaManager, final SidebarService sidebarService, final DefinitionToggleService definitionToggleService) {
        this.arenaManager = arenaManager;
        this.sidebarService = sidebarService;
        this.definitionToggleService = definitionToggleService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!sender.hasPermission("disasters.admin")) {
            Msg.send(sender, "no-permission");
            return true;
        }
        if (args.length == 0) {
            return false;
        }

        final String route = args[0].toLowerCase(Locale.ROOT);
        if ("reload".equals(route) && args.length == 1) {
            reload(sender);
            return true;
        }
        if ("setspawn".equals(route) && args.length == 1) {
            setSpawn(sender);
            return true;
        }
        if ("catalog".equals(route) && args.length == 2) {
            final String catalog = args[1].toLowerCase(Locale.ROOT);
            if ("disasters".equals(catalog)) {
                catalogDisasters(sender);
                return true;
            }
            if ("modifications".equals(catalog)) {
                catalogModifications(sender);
                return true;
            }
            return false;
        }
        if ("config".equals(route)) {
            return config(sender, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (!sender.hasPermission("disasters.admin") || args.length == 0) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return suggestions(Arrays.asList("reload", "setspawn", "catalog", "config"), args[0]);
        }
        if ("catalog".equalsIgnoreCase(args[0]) && args.length == 2) {
            return suggestions(Arrays.asList("disasters", "modifications"), args[1]);
        }
        if ("config".equalsIgnoreCase(args[0])) {
            if (args.length == 2) {
                return suggestions(Arrays.asList("disaster", "modification"), args[1]);
            }
            if (args.length >= 3) {
                final String kind = args[1].toLowerCase(Locale.ROOT);
                if (!("disaster".equals(kind) || "modification".equals(kind))) {
                    return Collections.emptyList();
                }
                if (args.length == 3) {
                    return suggestions(Arrays.asList("default", "arena"), args[2]);
                }
                final String scope = args[2].toLowerCase(Locale.ROOT);
                if ("default".equals(scope)) {
                    if (args.length == 4) {
                        return definitionSuggestions(kind, args[3]);
                    }
                    if (args.length == 5) {
                        return suggestions(Arrays.asList("true", "false"), args[4]);
                    }
                }
                if ("arena".equals(scope)) {
                    if (args.length == 4) {
                        return arenaSuggestions(args[3]);
                    }
                    if (args.length == 5) {
                        return definitionSuggestions(kind, args[4]);
                    }
                    if (args.length == 6) {
                        return suggestions(Arrays.asList("true", "false"), args[5]);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private void reload(final CommandSender sender) {
        FileManager.reload("config");
        FileManager.reload("lang");
        FileManager.reload("scoreboard");
        FileManager.reload("enabledisasters");
        FileManager.reload("deadmessages");
        FileManager.reload("item-drops");
        Lobby.setLocation();
        if (!arenaManager.reloadArenas()) {
            Msg.send(sender, "reload-blocked-active-arenas");
            return;
        }
        sidebarService.updateSidebar();
        ItemDropManager.loadConfig();
        Msg.send(sender, "reload-success");
    }

    private void setSpawn(final CommandSender sender) {
        if (!(sender instanceof Player)) return;
        final Player player = (Player) sender;
        FileManager.get("config").set("lobby.world", player.getWorld().getName());
        FileManager.get("config").set("lobby.x", player.getLocation().getX());
        FileManager.get("config").set("lobby.y", player.getLocation().getY());
        FileManager.get("config").set("lobby.z", player.getLocation().getZ());
        FileManager.get("config").set("lobby.yaw", (double) player.getLocation().getYaw());
        FileManager.get("config").set("lobby.pitch", (double) player.getLocation().getPitch());
        FileManager.get("config").save();
        FileManager.reload("config");
        Lobby.setLocation();
        Msg.send(sender, "lobby-set");
    }

    private void catalogDisasters(final CommandSender sender) {
        Msg.send(sender, "catalog-disasters-header");
        for (final DisasterDefinition definition : DisasterRegistry.allDefinitions()) {
            Msg.sendParsed(sender, definition.getId() + " - " + definition.getDisplayName());
        }
    }

    private void catalogModifications(final CommandSender sender) {
        Msg.send(sender, "catalog-modifications-header");
        for (final GameModificationDefinition definition : GameModificationRegistry.all()) {
            Msg.sendParsed(sender, definition.getId() + " - " + definition.getDisplayName());
        }
    }

    private boolean config(final CommandSender sender, final String[] args) {
        if (args.length < 5 || args.length > 6) {
            return false;
        }
        final String kind = args[1].toLowerCase(Locale.ROOT);
        final String scope = args[2].toLowerCase(Locale.ROOT);
        final boolean arenaScope = "arena".equals(scope);
        if (!("default".equals(scope) || arenaScope)) {
            return false;
        }
        if (!("disaster".equals(kind) || "modification".equals(kind))) {
            return false;
        }
        if (("default".equals(scope) && args.length != 5) || (arenaScope && args.length != 6)) {
            return false;
        }

        final int idIndex = arenaScope ? 4 : 3;
        final int booleanIndex = arenaScope ? 5 : 4;
        final Optional<Boolean> enabled = parseBoolean(args[booleanIndex]);
        if (!enabled.isPresent()) {
            sender.sendMessage(Component.text("Invalid boolean: " + args[booleanIndex] + " (expected true or false)", NamedTextColor.RED));
            return true;
        }

        if (arenaScope) {
            final Arena arena = arenaManager.getArena(args[3]);
            if (arena == null) {
                sender.sendMessage(Component.text("Unknown arena: " + args[3], NamedTextColor.RED));
                return true;
            }
            if ("disaster".equals(kind)) {
                final Optional<DisasterDefinition> disaster = DisasterRegistry.findDefinition(args[idIndex]);
                if (!disaster.isPresent()) {
                    sender.sendMessage(Component.text("Unknown disaster: " + args[idIndex], NamedTextColor.RED));
                    return true;
                }
                definitionToggleService.setArenaDisaster(arena, disaster.get().getId(), enabled.get());
                sendToggleSuccess(sender, "disaster", arena.getName(), disaster.get().getId(), enabled.get());
                return true;
            }
            if ("modification".equals(kind)) {
                final Optional<GameModificationDefinition> modification = GameModificationRegistry.find(args[idIndex]);
                if (!modification.isPresent()) {
                    sender.sendMessage(Component.text("Unknown game modification: " + args[idIndex], NamedTextColor.RED));
                    return true;
                }
                definitionToggleService.setArenaModification(arena, modification.get().getId(), enabled.get());
                sendToggleSuccess(sender, "modification", arena.getName(), modification.get().getId(), enabled.get());
                return true;
            }
            return false;
        }

        if ("disaster".equals(kind)) {
            final Optional<DisasterDefinition> disaster = DisasterRegistry.findDefinition(args[idIndex]);
            if (!disaster.isPresent()) {
                sender.sendMessage(Component.text("Unknown disaster: " + args[idIndex], NamedTextColor.RED));
                return true;
            }
            definitionToggleService.setDefaultDisaster(disaster.get().getId(), enabled.get());
            sendToggleSuccess(sender, "disaster", "default", disaster.get().getId(), enabled.get());
            return true;
        }
        if ("modification".equals(kind)) {
            final Optional<GameModificationDefinition> modification = GameModificationRegistry.find(args[idIndex]);
            if (!modification.isPresent()) {
                sender.sendMessage(Component.text("Unknown game modification: " + args[idIndex], NamedTextColor.RED));
                return true;
            }
            definitionToggleService.setDefaultModification(modification.get().getId(), enabled.get());
            sendToggleSuccess(sender, "modification", "default", modification.get().getId(), enabled.get());
            return true;
        }
        return false;
    }

    private Optional<Boolean> parseBoolean(final String raw) {
        if ("true".equalsIgnoreCase(raw)) {
            return Optional.of(Boolean.TRUE);
        }
        if ("false".equalsIgnoreCase(raw)) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }

    private List<String> definitionSuggestions(final String kind, final String prefix) {
        if ("disaster".equals(kind)) {
            return suggestions(DisasterRegistry.definitionIds(), prefix);
        }
        if ("modification".equals(kind)) {
            return suggestions(GameModificationRegistry.definitionIds(), prefix);
        }
        return Collections.emptyList();
    }

    private List<String> arenaSuggestions(final String prefix) {
        final List<String> names = new ArrayList<String>();
        for (final Arena arena : arenaManager.getArenas()) {
            names.add(arena.getName());
        }
        return suggestions(names, prefix);
    }

    private List<String> suggestions(final Iterable<String> values, final String prefix) {
        final String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        final List<String> matches = new ArrayList<String>();
        for (final String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix)) {
                matches.add(value);
            }
        }
        Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
        return matches;
    }

    private void sendToggleSuccess(final CommandSender sender, final String kind, final String scope, final String id, final boolean enabled) {
        final Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("%kind%", kind);
        replacements.put("%scope%", scope);
        replacements.put("%id%", id);
        replacements.put("%state%", enabled ? "enabled" : "disabled");
        Msg.send(sender, "definition-toggle-success", replacements);
    }
}
