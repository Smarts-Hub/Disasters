package me.hhitt.disasters.obj.block

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import net.minecraft.core.BlockPos
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import net.minecraft.world.level.block.Blocks

class DisasterFloor(private val arena: Arena, private val location: Location) {
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
            return
        }
        DisasterRegistry.removeBlockFromFloorIsLava(arena, this)
    }

    private fun setBlockMaterial(location: Location, material: Material) {
        val worldServer = (location.world as CraftWorld).handle
        val blockPosition = BlockPos(location.blockX, location.blockY, location.blockZ)
        val blockData = when (material) {
            Material.YELLOW_WOOL -> Blocks.YELLOW_WOOL.defaultBlockState()
            Material.ORANGE_WOOL -> Blocks.ORANGE_WOOL.defaultBlockState()
            Material.RED_WOOL -> Blocks.RED_WOOL.defaultBlockState()
            Material.LAVA -> Blocks.LAVA.defaultBlockState()
            else -> return
        }
        worldServer.setBlockAndUpdate(blockPosition, blockData)
    }
}

