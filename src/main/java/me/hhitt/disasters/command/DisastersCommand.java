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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.Map;

@Command("disasters")
@CommandPermission("disasters.admin")
public class DisastersCommand {

    private final ArenaManager arenaManager;
    private final SidebarService sidebarService;
    private final DefinitionToggleService definitionToggleService;

    public DisastersCommand(final ArenaManager arenaManager, final SidebarService sidebarService, final DefinitionToggleService definitionToggleService) {
        this.arenaManager = arenaManager;
        this.sidebarService = sidebarService;
        this.definitionToggleService = definitionToggleService;
    }

    @Subcommand("reload")
    public void reload(final BukkitCommandActor actor) {
        FileManager.reload("config");
        FileManager.reload("lang");
        FileManager.reload("scoreboard");
        FileManager.reload("enabledisasters");
        FileManager.reload("deadmessages");
        FileManager.reload("item-drops");
        Lobby.setLocation();
        if (!arenaManager.reloadArenas()) {
            Msg.send(actor.sender(), "reload-blocked-active-arenas");
            return;
        }
        sidebarService.updateSidebar();
        ItemDropManager.loadConfig();
        Msg.send(actor.sender(), "reload-success");
    }

    @Subcommand("setspawn")
    public void setSpawn(final BukkitCommandActor actor) {
        if (!actor.isPlayer()) return;
        final Player player = actor.asPlayer();
        FileManager.get("config").set("lobby.world", player.getWorld().getName());
        FileManager.get("config").set("lobby.x", player.getLocation().getX());
        FileManager.get("config").set("lobby.y", player.getLocation().getY());
        FileManager.get("config").set("lobby.z", player.getLocation().getZ());
        FileManager.get("config").set("lobby.yaw", (double) player.getLocation().getYaw());
        FileManager.get("config").set("lobby.pitch", (double) player.getLocation().getPitch());
        FileManager.get("config").save();
        FileManager.reload("config");
        Lobby.setLocation();
        Msg.send(actor.sender(), "lobby-set");
    }

    @Subcommand("catalog disasters")
    public void catalogDisasters(final BukkitCommandActor actor) {
        final CommandSender sender = actor.sender();
        Msg.send(sender, "catalog-disasters-header");
        for (final DisasterDefinition definition : DisasterRegistry.allDefinitions()) {
            Msg.sendParsed(sender, definition.getId() + " - " + definition.getDisplayName());
        }
    }

    @Subcommand("catalog modifications")
    public void catalogModifications(final BukkitCommandActor actor) {
        final CommandSender sender = actor.sender();
        Msg.send(sender, "catalog-modifications-header");
        for (final GameModificationDefinition definition : GameModificationRegistry.all()) {
            Msg.sendParsed(sender, definition.getId() + " - " + definition.getDisplayName());
        }
    }

    @Subcommand("config disaster default <disaster> <enabled>")
    public void setDefaultDisaster(final BukkitCommandActor actor, final DisasterDefinition disaster, final boolean enabled) {
        definitionToggleService.setDefaultDisaster(disaster.getId(), enabled);
        sendToggleSuccess(actor.sender(), "disaster", "default", disaster.getId(), enabled);
    }

    @Subcommand("config disaster arena <arena> <disaster> <enabled>")
    public void setArenaDisaster(final BukkitCommandActor actor, final Arena arena, final DisasterDefinition disaster, final boolean enabled) {
        definitionToggleService.setArenaDisaster(arena, disaster.getId(), enabled);
        sendToggleSuccess(actor.sender(), "disaster", arena.getName(), disaster.getId(), enabled);
    }

    @Subcommand("config modification default <modification> <enabled>")
    public void setDefaultModification(final BukkitCommandActor actor, final GameModificationDefinition modification, final boolean enabled) {
        definitionToggleService.setDefaultModification(modification.getId(), enabled);
        sendToggleSuccess(actor.sender(), "modification", "default", modification.getId(), enabled);
    }

    @Subcommand("config modification arena <arena> <modification> <enabled>")
    public void setArenaModification(final BukkitCommandActor actor, final Arena arena, final GameModificationDefinition modification, final boolean enabled) {
        definitionToggleService.setArenaModification(arena, modification.getId(), enabled);
        sendToggleSuccess(actor.sender(), "modification", arena.getName(), modification.getId(), enabled);
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
