package me.hhitt.disasters.command.parameter;

import me.hhitt.disasters.disaster.DisasterDefinition;
import me.hhitt.disasters.disaster.DisasterRegistry;
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
import java.util.Optional;

public final class DisasterDefinitionParameterType implements ParameterType<BukkitCommandActor, DisasterDefinition> {

    @Override
    public DisasterDefinition parse(final MutableStringStream input, final ExecutionContext<BukkitCommandActor> context) {
        final String value = input.readUnquotedString();
        final Optional<DisasterDefinition> definition = DisasterRegistry.findDefinition(value);
        if (!definition.isPresent()) {
            throw new CommandErrorException("Unknown disaster: " + value);
        }
        return definition.get();
    }

    @Override
    public SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return new SuggestionProvider<BukkitCommandActor>() {
            @Override
            public List<String> getSuggestions(final ExecutionContext<BukkitCommandActor> context) {
                final List<String> ids = new ArrayList<String>(DisasterRegistry.definitionIds());
                Collections.sort(ids, String.CASE_INSENSITIVE_ORDER);
                return ids;
            }
        };
    }

    @Override
    public PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}
