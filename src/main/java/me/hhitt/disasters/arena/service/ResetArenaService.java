package me.hhitt.disasters.arena.service;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.game.GameState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class ResetArenaService {

    private static final long WARN_THRESHOLD_MS = 5000L;

    private final Arena arena;
    private final WorldEditPlugin worldEdit;
    private final World world;
    private final Logger logger;
    private Clipboard clipboard;
    private BlockVector3 center;
    private boolean clipboardInitialized;

    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;

    public ResetArenaService(final Arena arena, final WorldEditPlugin worldEdit) {
        this.arena = arena;
        this.worldEdit = worldEdit;
        this.world = arena.getCorner1().getWorld();
        this.logger = Disasters.getInstance().getLogger();

        this.minX = (int) Math.min(arena.getCorner1().getX(), arena.getCorner2().getX());
        this.maxX = (int) Math.max(arena.getCorner1().getX(), arena.getCorner2().getX());
        this.minY = (int) Math.min(arena.getCorner1().getY(), arena.getCorner2().getY());
        this.maxY = (int) Math.max(arena.getCorner1().getY(), arena.getCorner2().getY());
        this.minZ = (int) Math.min(arena.getCorner1().getZ(), arena.getCorner2().getZ());
        this.maxZ = (int) Math.max(arena.getCorner1().getZ(), arena.getCorner2().getZ());
    }

    public void save() {
        final World world = arena.getCorner1().getWorld();
        if (world == null) {
            logger.severe("World is null before save!");
            return;
        }
        final BlockVector3 min = BlockVector3.at(arena.getCorner1().getX(), arena.getCorner1().getY(), arena.getCorner1().getZ());
        final BlockVector3 max = BlockVector3.at(arena.getCorner2().getX(), arena.getCorner2().getY(), arena.getCorner2().getZ());
        final CuboidRegion region = new CuboidRegion(min, max);
        final long start = System.nanoTime();
        logger.info("Saving arena '" + arena.getName() + "' blocks: " + region.getVolume());

        final BlockArrayClipboard clip = new BlockArrayClipboard(region);
        try (com.sk89q.worldedit.EditSession editSession = worldEdit.getWorldEdit().newEditSession(BukkitAdapter.adapt(world))) {
            final ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clip, region.getMinimumPoint());
            Operations.complete(forwardExtentCopy);
            this.clipboard = clip;
            this.center = region.getMinimumPoint();
            this.clipboardInitialized = true;
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
        final long elapsed = (System.nanoTime() - start) / 1_000_000L;
        if (elapsed >= WARN_THRESHOLD_MS) {
            logger.warning("Save completed for '" + arena.getName() + "' in " + elapsed + " ms (slow)");
        } else {
            logger.info("Save completed for '" + arena.getName() + "' in " + elapsed + " ms");
        }
    }

    public void paste() {
        if (!clipboardInitialized) {
            logger.severe("Clipboard not initialized!");
            return;
        }
        logger.info("Paste triggered for '" + arena.getName() + "'");
        final long start = System.nanoTime();
        removeEntitiesInRegion();

        try (com.sk89q.worldedit.EditSession editSession = worldEdit.getWorldEdit().newEditSession(BukkitAdapter.adapt(world))) {
            logger.info("Pasting arena '" + arena.getName() + "' now...");
            final com.sk89q.worldedit.function.operation.Operation operation = new ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(center)
                .ignoreAirBlocks(false)
                .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
        refreshChunks(world, arena.getCorner1(), arena.getCorner2());
        arena.setState(GameState.RECRUITING);
        final long elapsed = (System.nanoTime() - start) / 1_000_000L;
        if (elapsed >= WARN_THRESHOLD_MS) {
            logger.warning("Paste completed for '" + arena.getName() + "' in " + elapsed + " ms (slow)");
        } else {
            logger.info("Paste completed for '" + arena.getName() + "' in " + elapsed + " ms");
        }
    }

    private void refreshChunks(final World world, final Location loc1, final Location loc2) {
        final int minXX = minX >> 4;
        final int maxXX = maxX >> 4;
        final int minZZ = minZ >> 4;
        final int maxZZ = maxZ >> 4;

        for (int x = minXX; x <= maxXX; x++) {
            for (int z = minZZ; z <= maxZZ; z++) {
                world.refreshChunk(x, z);
            }
        }
    }

    private void removeEntitiesInRegion() {
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            final Location loc = entity.getLocation();
            if (loc.getBlockX() >= minX && loc.getBlockX() <= maxX
                && loc.getBlockY() >= minY && loc.getBlockY() <= maxY
                && loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }
}
