package me.hhitt.disasters.disaster.rising;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HorizontalColumnCursorTest {

    @Test
    void rejectsInvertedXBounds() {
        assertThrows(IllegalArgumentException.class, () -> new HorizontalColumnCursor(2, 1, 0, 0));
    }

    @Test
    void rejectsInvertedZBounds() {
        assertThrows(IllegalArgumentException.class, () -> new HorizontalColumnCursor(0, 0, 2, 1));
    }

    @Test
    void countsInclusiveColumns() {
        final HorizontalColumnCursor cursor = new HorizontalColumnCursor(10, 12, 20, 21);

        assertEquals(6, cursor.getTotalColumns());
    }

    @Test
    void packsAndUnpacksCoordinates() {
        final HorizontalColumnCursor cursor = new HorizontalColumnCursor(10, 12, 20, 21);

        final int packed = cursor.pack(11, 21);

        assertEquals(11, cursor.unpackX(packed));
        assertEquals(21, cursor.unpackZ(packed));
    }

    @Test
    void iteratesRowMajorXThenZ() {
        final HorizontalColumnCursor cursor = new HorizontalColumnCursor(10, 11, 20, 21);

        assertEquals(cursor.pack(10, 20), cursor.nextPacked());
        assertEquals(cursor.pack(11, 20), cursor.nextPacked());
        assertEquals(cursor.pack(10, 21), cursor.nextPacked());
        assertEquals(cursor.pack(11, 21), cursor.nextPacked());
    }

    @Test
    void resetRewindsAndExhaustionThrows() {
        final HorizontalColumnCursor cursor = new HorizontalColumnCursor(0, 0, 0, 0);

        assertEquals(0, cursor.nextPacked());
        assertThrows(NoSuchElementException.class, cursor::nextPacked);
        cursor.reset();
        assertEquals(0, cursor.nextPacked());
    }
}
