package me.hhitt.disasters.model.block;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.DisasterRegistry;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Objects;

public class DisappearBlock {

    private static final int MAX_STAGE = 9;
    private static final int STAGE_INCREMENT = 2;

    private final Arena arena;
    private final Location location;
    private final int sourceId;
    private int currentStage = -1;
    private boolean occupied = true;

    public DisappearBlock(Arena arena, Location location) {
        this.arena = arena;
        this.location = location;
        this.sourceId = Objects.hash(
            location.getWorld() != null ? location.getWorld().getUID() : null,
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }

    public Location getLocation() {
        return location;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void updateMaterial() {
        if (!occupied) {
            return;
        }
        currentStage += STAGE_INCREMENT;
        if (currentStage <= MAX_STAGE) {
            sendCrackAnimation(currentStage);
        } else {
            clearCrackAnimation();
            setBlockToAir();
            DisasterRegistry.removeBlockFromDisappear(arena, this);
        }
    }

    private void sendCrackAnimation(int stage) {
        float progress = Math.max(0.0F, Math.min(1.0F, (float) stage / (float) MAX_STAGE));
        arena.getPlaying().forEach(player -> player.sendBlockDamage(location, progress, sourceId));
    }

    private void clearCrackAnimation() {
        arena.getPlaying().forEach(player -> player.sendBlockDamage(location, 0.0F, sourceId));
    }

    private void setBlockToAir() {
        location.getBlock().setType(Material.AIR, false);
    }
}
