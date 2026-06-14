package me.hhitt.disasters.command.parameter;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import org.junit.jupiter.api.Test;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.node.ExecutionContext;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArenaParameterTypeTest {

    @Test
    void defaultSuggestionsReturnSortedArenaNames() {
        final ArenaManager arenaManager = mock(ArenaManager.class);
        final Arena beta = mock(Arena.class);
        final Arena alpha = mock(Arena.class);
        when(beta.getName()).thenReturn("beta");
        when(alpha.getName()).thenReturn("Alpha");
        when(arenaManager.getArenas()).thenReturn(Arrays.asList(beta, alpha));

        @SuppressWarnings("unchecked")
        final ExecutionContext<BukkitCommandActor> context = mock(ExecutionContext.class);
        final Collection<String> suggestions = new ArenaParameterType(arenaManager)
            .defaultSuggestions()
            .getSuggestions(context);

        assertEquals(Arrays.asList("Alpha", "beta"), suggestions);
    }
}
