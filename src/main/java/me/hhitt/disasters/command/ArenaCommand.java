package me.hhitt.disasters.command;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.game.ForceStartResult;
import me.hhitt.disasters.util.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.HashMap;
import java.util.Map;

@Command("arena")
public final class ArenaCommand {

    private final ArenaManager arenaManager;

    public ArenaCommand(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Subcommand("join <arena>")
    public void join(final BukkitCommandActor actor, final Arena arena) {
        if (!actor.isPlayer()) return;
        final Player player = actor.asPlayer();

        if (arenaManager.getArena(player) != null) {
            Msg.send(player, "already-in-arena");
            return;
        }

        if (!arena.addPlayer(player)) {
            if (arena.isFull()) {
                Msg.send(player, "arena-full");
            } else {
                Msg.send(player, "arena-in-game");
            }
        }
    }

    @Subcommand("quickjoin")
    public void quickJoin(final BukkitCommandActor actor) {
        if (!actor.isPlayer()) return;
        final Player player = actor.asPlayer();

        if (arenaManager.getArena(player) != null) {
            Msg.send(player, "already-in-arena");
            return;
        }

        if (!arenaManager.addPlayerToBestArena(player)) {
            Msg.send(player, "no-available-arena");
        }
    }

    @Subcommand("leave")
    public void leave(final BukkitCommandActor actor) {
        if (!actor.isPlayer()) return;
        final Player player = actor.asPlayer();

        final Arena arena = arenaManager.getArena(player);
        if (arena != null) {
            arena.removePlayer(player);
        } else {
            Msg.send(player, "not-in-arena");
        }
    }

    @Subcommand("forcestart")
    public void forceStart(final BukkitCommandActor actor) {
        if (!actor.isPlayer()) return;
        final Player player = actor.asPlayer();

        final Arena arena = arenaManager.getArena(player);
        if (arena != null) {
            if (!player.hasPermission("disasters.forcestart")) {
                Msg.send(player, "no-permission");
                return;
            }
            sendForceStartResult(player, arena, arena.forceStart());
        } else {
            Msg.send(player, "not-in-arena");
        }
    }

    @Subcommand("forcestop")
    public void forceStop(final BukkitCommandActor actor) {
        if (!actor.isPlayer()) return;
        final Player player = actor.asPlayer();

        final Arena arena = arenaManager.getArena(player);
        if (arena != null) {
            if (!player.hasPermission("disasters.forcestop")) {
                Msg.send(player, "no-permission");
                return;
            }
            arena.stop();
        } else {
            Msg.send(player, "not-in-arena");
        }
    }

    @Subcommand("forcestart <arena>")
    public void forceStart(final BukkitCommandActor actor, final Arena arena) {
        final CommandSender sender = actor.sender();

        if (!sender.hasPermission("disasters.forcestart")) {
            Msg.send(sender, "no-permission");
            return;
        }
        sendForceStartResult(sender, arena, arena.forceStart());
    }

    @Subcommand("forcestop <arena>")
    public void forceStop(final BukkitCommandActor actor, final Arena arena) {
        final CommandSender sender = actor.sender();

        if (!sender.hasPermission("disasters.forcestop")) {
            Msg.send(sender, "no-permission");
            return;
        }
        arena.stop();
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
