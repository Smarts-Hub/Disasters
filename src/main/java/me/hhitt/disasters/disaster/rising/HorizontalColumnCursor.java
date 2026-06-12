package me.hhitt.disasters.disaster.rising;

import java.util.NoSuchElementException;

public final class HorizontalColumnCursor {

    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;
    private final int width;
    private final int depth;
    private final int totalColumns;
    private int nextIndex;

    public HorizontalColumnCursor(final int minX, final int maxX, final int minZ, final int maxZ) {
        if (maxX < minX) {
            throw new IllegalArgumentException("maxX must be >= minX");
        }
        if (maxZ < minZ) {
            throw new IllegalArgumentException("maxZ must be >= minZ");
        }
        final long widthLong = (long) maxX - minX + 1L;
        final long depthLong = (long) maxZ - minZ + 1L;
        final long totalLong = widthLong * depthLong;
        if (widthLong > Integer.MAX_VALUE || depthLong > Integer.MAX_VALUE || totalLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("column bounds are too large");
        }
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.width = (int) widthLong;
        this.depth = (int) depthLong;
        this.totalColumns = (int) totalLong;
    }

    public void reset() {
        nextIndex = 0;
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public int getTotalColumns() {
        return totalColumns;
    }

    public int nextPacked() {
        if (nextIndex >= totalColumns) {
            throw new NoSuchElementException("cursor exhausted");
        }
        final int index = nextIndex++;
        final int x = minX + (index % width);
        final int z = minZ + (index / width);
        return pack(x, z);
    }

    public int pack(final int x, final int z) {
        if (x < minX || x > maxX || z < minZ || z > maxZ) {
            throw new IllegalArgumentException("coordinate outside cursor bounds");
        }
        final int relativeX = x - minX;
        final int relativeZ = z - minZ;
        return relativeZ * width + relativeX;
    }

    public int unpackX(final int packed) {
        validatePacked(packed);
        return minX + (packed % width);
    }

    public int unpackZ(final int packed) {
        validatePacked(packed);
        return minZ + (packed / width);
    }

    private void validatePacked(final int packed) {
        if (packed < 0 || packed >= totalColumns) {
            throw new IllegalArgumentException("packed coordinate outside cursor bounds");
        }
    }
}
