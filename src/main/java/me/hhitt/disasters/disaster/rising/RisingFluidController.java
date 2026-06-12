package me.hhitt.disasters.disaster.rising;

import me.hhitt.disasters.arena.Arena;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public final class RisingFluidController {

    private final World world;
    private final Material fluid;
    private final int minX;
    private final int minZ;
    private final int maxY;
    private final int riseIntervalSeconds;
    private final int maxColumnsPerPulse;
    private final HorizontalColumnCursor cursor;

    private int currentY;
    private int nextLayerStartSecond;
    private boolean layerActive;

    public RisingFluidController(
        final Arena arena,
        final Material fluid,
        final int startY,
        final int riseIntervalSeconds,
        final int maxColumnsPerPulse
    ) {
        Objects.requireNonNull(arena, "arena");
        this.world = Objects.requireNonNull(arena.getCorner1().getWorld(), "arena corner world");
        this.fluid = Objects.requireNonNull(fluid, "fluid");
        this.minX = Math.min(arena.getCorner1().getBlockX(), arena.getCorner2().getBlockX());
        final int maxX = Math.max(arena.getCorner1().getBlockX(), arena.getCorner2().getBlockX());
        this.minZ = Math.min(arena.getCorner1().getBlockZ(), arena.getCorner2().getBlockZ());
        final int maxZ = Math.max(arena.getCorner1().getBlockZ(), arena.getCorner2().getBlockZ());
        final int regionMaxY = Math.max(arena.getCorner1().getBlockY(), arena.getCorner2().getBlockY());
        this.maxY = Math.min(regionMaxY, world.getMaxHeight() - 1);
        this.riseIntervalSeconds = Math.max(1, riseIntervalSeconds);
        this.maxColumnsPerPulse = Math.max(1, maxColumnsPerPulse);
        this.cursor = new HorizontalColumnCursor(minX, maxX, minZ, maxZ);
        this.currentY = startY;
        this.nextLayerStartSecond = this.riseIntervalSeconds;
    }

    public boolean pulse(final int elapsedSeconds) {
        if (currentY > maxY) {
            return false;
        }
        if (!layerActive) {
            if (elapsedSeconds < nextLayerStartSecond) {
                return true;
            }
            layerActive = true;
            cursor.reset();
        }

        int inspected = 0;
        while (inspected < maxColumnsPerPulse && cursor.getNextIndex() < cursor.getTotalColumns()) {
            final int packed = cursor.nextPacked();
            final int x = cursor.unpackX(packed);
            final int z = cursor.unpackZ(packed);
            final Block block = world.getBlockAt(x, currentY, z);
            final Material type = block.getType();
            if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR) {
                block.setType(fluid);
            }
            inspected++;
        }

        if (cursor.getNextIndex() >= cursor.getTotalColumns()) {
            currentY++;
            layerActive = false;
            nextLayerStartSecond = elapsedSeconds + riseIntervalSeconds;
        }
        return currentY <= maxY;
    }

    public int getRiseIntervalSeconds() {
        return riseIntervalSeconds;
    }

    public int getCurrentY() {
        return currentY;
    }

    public int getMaxY() {
        return maxY;
    }
}
