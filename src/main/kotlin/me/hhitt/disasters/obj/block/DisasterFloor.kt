package me.hhitt.disasters.obj.block

import net.minecraft.core.BlockPos
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld

class DisasterFloor(private val location: Location) {
    private val materials = listOf(
        Material.YELLOW_WOOL,
        Material.ORANGE_WOOL,
        Material.RED_WOOL,
        Material.LAVA
    )
    private var currentStage = 0

    fun updateMaterial() {
        if (currentStage < materials.size) {
            setBlockMaterial(location, materials[currentStage])
            currentStage++
        }
    }

    private fun setBlockMaterial(location: Location, material: Material) {
        val worldServer = (location.world as CraftWorld).handle
        val blockPosition = BlockPos(location.blockX, location.blockY, location.blockZ)
    }
}
