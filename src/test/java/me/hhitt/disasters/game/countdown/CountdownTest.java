package me.hhitt.disasters.game.countdown;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.game.GameSession;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CountdownTest {

    @Test
    void blockedCountdownReturnsBeforeTickProcessing() {
        final Arena arena = mock(Arena.class);
        when(arena.getCountdown()).thenReturn(10);

        final GameSession session = mock(GameSession.class);
        when(session.cancelCountdownIfBlocked()).thenReturn(true);

        final Countdown countdown = new Countdown(arena, session);

        countdown.run();

        verify(session).cancelCountdownIfBlocked();
        verifyNoMoreInteractions(session);
    }
}
