package me.hhitt.disasters.command.parameter;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ArenaParameterType implements ParameterType<BukkitCommandActor, Arena> {

    private final ArenaManager arenaManager;

    public ArenaParameterType(final ArenaManager arenaManager) {
        this.arenaManager = Objects.requireNonNull(arenaManager, "arenaManager");
    }

    @Override
    public Arena parse(final MutableStringStream input, final ExecutionContext<BukkitCommandActor> context) {
        final String value = input.readUnquotedString();
        final Arena arena = arenaManager.getArena(value);
        if (arena == null) {
            throw new CommandErrorException("Unknown arena: " + value);
        }
        return arena;
    }

    @Override
    public SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return new SuggestionProvider<BukkitCommandActor>() {
            @Override
            public List<String> getSuggestions(final ExecutionContext<BukkitCommandActor> context) {
                final List<String> names = new ArrayList<String>();
                for (final Arena arena : arenaManager.getArenas()) {
                    names.add(arena.getName());
                }
                Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
                return names;
            }
        };
    }

    @Override
    public PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}
