package me.hhitt.disasters.model.block;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.DisasterRegistry;
import org.bukkit.Location;
import org.bukkit.Material;

public class DisasterFloor {

    private static final Material[] MATERIALS = {
        Material.YELLOW_WOOL,
        Material.ORANGE_WOOL,
        Material.RED_WOOL,
        Material.LAVA
    };

    private final Arena arena;
    private final Location location;
    private int currentStage = 0;

    public DisasterFloor(Arena arena, Location location) {
        this.arena = arena;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void updateMaterial() {
        if (currentStage < MATERIALS.length) {
            setBlockMaterial(location, MATERIALS[currentStage]);
            currentStage++;
            return;
        }
        DisasterRegistry.removeBlockFromFloorIsLava(arena, this);
    }

    private void setBlockMaterial(Location location, Material material) {
        location.getBlock().setType(material, false);
    }
}
