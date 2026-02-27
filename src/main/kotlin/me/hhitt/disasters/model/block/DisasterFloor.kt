package me.hhitt.disasters.model.block

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import net.minecraft.world.level.block.Blocks
import org.bukkit.craftbukkit.entity.CraftPlayer

/**
 * DisasterFloor is a class that represents a floor block in the arena that changes its material
 * based on the current stage of the disaster.
 *
 * @param arena The arena where the disaster is taking place.
 * @param location The location of the block in the arena.
 */

class DisasterFloor(private val arena: Arena, val location: Location) {
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
        // Location already points to the block below the player (fixed in DisasterRegistry)
        val blockPosition = BlockPos(location.blockX, location.blockY, location.blockZ)
        val blockData = when (material) {
            Material.YELLOW_WOOL -> Blocks.YELLOW_WOOL.defaultBlockState()
            Material.ORANGE_WOOL -> Blocks.ORANGE_WOOL.defaultBlockState()
            Material.RED_WOOL -> Blocks.RED_WOOL.defaultBlockState()
            Material.LAVA -> Blocks.LAVA.defaultBlockState()
            else -> return
        }
        worldServer.setBlockAndUpdate(blockPosition, blockData)
        val packet = ClientboundBlockUpdatePacket(worldServer, blockPosition)
        arena.playing.forEach { player -> (player as CraftPlayer).handle.connection.send(packet) }
    }
}