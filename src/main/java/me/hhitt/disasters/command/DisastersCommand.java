package me.hhitt.disasters.command;

import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.game.drop.ItemDropManager;
import me.hhitt.disasters.sidebar.SidebarService;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Lobby;
import me.hhitt.disasters.util.Msg;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("disasters")
@CommandPermission("disasters.admin")
public class DisastersCommand {

    private final ArenaManager arenaManager;
    private final SidebarService sidebarService;

    public DisastersCommand(final ArenaManager arenaManager, final SidebarService sidebarService) {
        this.arenaManager = arenaManager;
        this.sidebarService = sidebarService;
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
}
