package me.hhitt.disasters.game.modification.vote;

import me.hhitt.disasters.game.modification.GameModificationDefinition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class GameModificationSelectionPolicy {

    private GameModificationSelectionPolicy() {
    }

    public static List<String> select(
        final List<GameModificationDefinition> orderedEnabled,
        final Map<UUID, String> votes,
        final boolean allowNoVoteDefault,
        final String defaultId
    ) {
        final LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (final GameModificationDefinition def : orderedEnabled) {
            counts.put(normalizeId(def.getId()), Integer.valueOf(0));
        }
        for (final String voteId : votes.values()) {
            final String normalizedVoteId = normalizeId(voteId);
            if (!counts.containsKey(normalizedVoteId)) {
                continue;
            }
            counts.put(normalizedVoteId, Integer.valueOf(counts.get(normalizedVoteId).intValue() + 1));
        }

        String winner = null;
        int winningCount = 0;
        for (final GameModificationDefinition def : orderedEnabled) {
            final String id = normalizeId(def.getId());
            final int count = counts.get(id).intValue();
            if (count > winningCount) {
                winner = id;
                winningCount = count;
            }
        }
        if (winner != null) {
            return Collections.singletonList(winner);
        }

        if (!allowNoVoteDefault) {
            return Collections.emptyList();
        }
        final String normalizedDefaultId = normalizeId(defaultId);
        if (!normalizedDefaultId.isEmpty() && counts.containsKey(normalizedDefaultId)) {
            return Collections.singletonList(normalizedDefaultId);
        }
        return Collections.emptyList();
    }

    private static String normalizeId(final String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }
}
