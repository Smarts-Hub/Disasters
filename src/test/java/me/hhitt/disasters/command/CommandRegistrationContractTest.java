package me.hhitt.disasters.command;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRegistrationContractTest {

    @Test
    void pluginDescriptorDeclaresArenaCommand() {
        final PluginDescriptionFile description = loadPluginDescription();

        assertTrue(description.getCommands().containsKey("arena"));
    }

    @Test
    void pluginDescriptorDeclaresDisastersCommand() {
        final PluginDescriptionFile description = loadPluginDescription();

        assertTrue(description.getCommands().containsKey("disasters"));
    }

    @Test
    void pluginDescriptorTargetsPaper26_2() {
        final PluginDescriptionFile description = loadPluginDescription();

        assertEquals("26.2", description.getAPIVersion());
    }

    private static PluginDescriptionFile loadPluginDescription() {
        final InputStream stream = CommandRegistrationContractTest.class
            .getClassLoader()
            .getResourceAsStream("plugin.yml");
        if (stream == null) {
            throw new IllegalStateException("plugin.yml resource is missing");
        }

        try (InputStream input = stream) {
            return new PluginDescriptionFile(input);
        } catch (IOException | InvalidDescriptionException exception) {
            throw new IllegalStateException("plugin.yml resource is invalid", exception);
        }
    }
}
