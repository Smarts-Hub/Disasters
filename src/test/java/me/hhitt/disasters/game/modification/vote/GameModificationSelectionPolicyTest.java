package me.hhitt.disasters.game.modification.vote;

import me.hhitt.disasters.game.modification.GameModificationRegistry;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameModificationSelectionPolicyTest {

    @Test
    void highestValidVoteWins() {
        final Map<UUID, String> votes = new LinkedHashMap<UUID, String>();
        votes.put(UUID.randomUUID(), "pvp");
        votes.put(UUID.randomUUID(), "swapper");
        votes.put(UUID.randomUUID(), "PVP");
        votes.put(UUID.randomUUID(), "unknown");

        assertEquals(list("pvp"), select(votes, true, "one-heart"));
    }

    @Test
    void tieBreaksByEnabledOrder() {
        final Map<UUID, String> votes = new LinkedHashMap<UUID, String>();
        votes.put(UUID.randomUUID(), "pvp");
        votes.put(UUID.randomUUID(), "swapper");

        assertEquals(list("swapper"), select(votes, true, "one-heart"));
    }

    @Test
    void validDefaultSelectedWhenNoValidVotes() {
        assertEquals(list("one-heart"), select(new LinkedHashMap<UUID, String>(), true, "one-heart"));
    }

    @Test
    void invalidDefaultReturnsEmptySelection() {
        assertEquals(list(), select(new LinkedHashMap<UUID, String>(), true, "missing"));
    }

    @Test
    void disabledDefaultReturnsEmptySelection() {
        assertEquals(list(), select(new LinkedHashMap<UUID, String>(), false, "one-heart"));
    }

    @Test
    void enabledPvpWithoutPvpVotesOrDefaultDoesNotSelectPvp() {
        assertEquals(list("one-heart"), select(new LinkedHashMap<UUID, String>(), true, "one-heart"));
    }

    @Test
    void resultCannotMutate() {
        assertThrows(UnsupportedOperationException.class, () -> select(new LinkedHashMap<UUID, String>(), true, "one-heart").add("pvp"));
    }

    private static List<String> select(final Map<UUID, String> votes, final boolean allowDefault, final String defaultId) {
        return GameModificationSelectionPolicy.select(GameModificationRegistry.all(), votes, allowDefault, defaultId);
    }

    private static List<String> list(final String... ids) {
        return java.util.Arrays.asList(ids);
    }
}
