package me.hhitt.disasters.storage.data;

import java.util.Objects;

public final class PlayerStats {
    private final int wins;
    private final int defeats;
    private final int totalPlayed;

    public PlayerStats(final int wins, final int defeats, final int totalPlayed) {
        this.wins = wins;
        this.defeats = defeats;
        this.totalPlayed = totalPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getDefeats() {
        return defeats;
    }

    public int getTotalPlayed() {
        return totalPlayed;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PlayerStats)) {
            return false;
        }
        final PlayerStats that = (PlayerStats) object;
        return wins == that.wins && defeats == that.defeats && totalPlayed == that.totalPlayed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(wins, defeats, totalPlayed);
    }

    @Override
    public String toString() {
        return "PlayerStats(wins=" + wins
            + ", defeats=" + defeats
            + ", totalPlayed=" + totalPlayed
            + ')';
    }
}
