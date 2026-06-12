package me.hhitt.disasters.game.modification;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameModificationRegistryTest {

    @Test
    void idsAreUnique() {
        final Set<String> ids = new HashSet<String>();
        for (final String id : GameModificationRegistry.definitionIds()) {
            assertTrue(ids.add(id));
        }
        assertEquals(GameModificationRegistry.all().size(), ids.size());
    }

    @Test
    void findNormalizesId() {
        assertTrue(GameModificationRegistry.find("PVP").isPresent());
        assertEquals("pvp", GameModificationRegistry.find("PVP").get().getId());
    }

    @Test
    void orderIsStable() {
        assertEquals(java.util.Arrays.asList("one-heart", "swapper", "pvp"), GameModificationRegistry.definitionIds());
    }

    @Test
    void returnedViewsCannotMutate() {
        assertThrows(UnsupportedOperationException.class, () -> GameModificationRegistry.all().clear());
        assertThrows(UnsupportedOperationException.class, () -> GameModificationRegistry.definitionIds().add("other"));

        final List<String> description = GameModificationRegistry.find("pvp").get().getDescription();
        assertThrows(UnsupportedOperationException.class, () -> description.add("other"));
    }
}
