package me.hhitt.disasters.command;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArenaCommandTest {

    @Test
    void joinTabCompletionReturnsSortedArenaNames() {
        final ArenaManager arenaManager = mock(ArenaManager.class);
        final Arena beta = mock(Arena.class);
        final Arena alpha = mock(Arena.class);
        when(beta.getName()).thenReturn("beta");
        when(alpha.getName()).thenReturn("Alpha");
        when(arenaManager.getArenas()).thenReturn(Arrays.asList(beta, alpha));

        final CommandSender sender = mock(CommandSender.class);
        final Command command = mock(Command.class);
        final List<String> result = new ArenaCommand(arenaManager)
            .onTabComplete(sender, command, "arena", new String[]{"join", ""});

        assertEquals(Arrays.asList("Alpha", "beta"), result);
    }

    @Test
    void unknownArenaErrorIncludesRequestedArenaId() {
        final ArenaManager arenaManager = mock(ArenaManager.class);
        when(arenaManager.getArena("missing")).thenReturn(null);

        final CommandSender sender = mock(CommandSender.class);
        final Command command = mock(Command.class);
        final ArenaCommand subject = new ArenaCommand(arenaManager);

        subject.onCommand(sender, command, "arena", new String[]{"forcestart", "missing"});

        verify(sender).sendMessage(Component.text("Unknown arena: missing", NamedTextColor.RED));
    }
}
