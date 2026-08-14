package me.hhitt.disasters.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartFailureMessageContractTest {

    @Test
    void countdownFailureMessagesExposeRequiredValues() {
        final YamlConfiguration lang = loadLang();

        final String notEnoughPlayers = lang.getString("countdown-canceled.not-enough-players.subtitle");
        assertNotNull(notEnoughPlayers, "countdown-canceled.not-enough-players.subtitle is missing");
        assertTrue(notEnoughPlayers.contains("%current_players%"));
        assertTrue(notEnoughPlayers.contains("%min_players%"));

        final String endThreshold = lang.getString("countdown-canceled.end-threshold.subtitle");
        assertNotNull(endThreshold, "countdown-canceled.end-threshold.subtitle is missing");
        assertTrue(endThreshold.contains("%current_players%"));
        assertTrue(endThreshold.contains("%alive_to_end%"));
        assertTrue(endThreshold.contains("%required_players%"));
    }

    @Test
    void forceStartThresholdMessageExposesConfigurationCause() {
        final YamlConfiguration lang = loadLang();

        final String message = lang.getString("messages.force-start-insufficient-players");
        assertNotNull(message, "messages.force-start-insufficient-players is missing");
        assertTrue(message.contains("%arena%"));
        assertTrue(message.contains("%current_players%"));
        assertTrue(message.contains("%alive_to_end%"));
        assertTrue(message.contains("%required_players%"));
    }

    @Test
    void genericCountdownWaitMessageIsRemoved() {
        final YamlConfiguration lang = loadLang();

        assertFalse(lang.contains("countdown-canceled.subtitle"));
    }

    private static YamlConfiguration loadLang() {
        final InputStream stream = StartFailureMessageContractTest.class
            .getClassLoader()
            .getResourceAsStream("lang.yml");
        if (stream == null) {
            throw new IllegalStateException("lang.yml resource is missing");
        }

        try (InputStream input = stream) {
            return YamlConfiguration.loadConfiguration(
                new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("lang.yml resource could not be read", exception);
        }
    }
}
