package me.hhitt.disasters.model.block

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.DisasterRegistry
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.world.level.block.Blocks
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer

class DisappearBlock(private val arena: Arena, val location: Location) {

    // 10 crack stages (0-9), then block breaks to air
    // Advances 2 stages per pulse (1 pulse = 1 second) so full break takes ~5 seconds
    private val maxStage = 9
    private val stageIncrement = 2
    private var currentStage = -1
    // Whether a player is currently standing on this block (controls pause/resume)
    var occupied = true

    /**
     * Each entity ID must be unique per block destruction overlay.
     * Using the block's hash as a stable ID so the client knows which block to crack.
     */
    private val entityId = location.hashCode()

    fun updateMaterial() {
        // Only advance crack stages while a player is standing on the block
        if (!occupied) return

        currentStage += stageIncrement
        if (currentStage <= maxStage) {
            sendCrackAnimation(currentStage)
        } else {
            // Cracking complete — destroy the block
            clearCrackAnimation()
            setBlockToAir()
            DisasterRegistry.removeBlockFromDisappear(arena, this)
        }
    }

    /**
     * Sends the block crack overlay packet (same visual as when mining a block by hand).
     * Stage 0 = first cracks, stage 9 = about to break.
     */
    private fun sendCrackAnimation(stage: Int) {
        val blockPos = BlockPos(location.blockX, location.blockY, location.blockZ)
        val packet = ClientboundBlockDestructionPacket(entityId, blockPos, stage)
        arena.playing.forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }

    /**
     * Clears the crack overlay by sending stage -1 (removes the texture).
     */
    private fun clearCrackAnimation() {
        val blockPos = BlockPos(location.blockX, location.blockY, location.blockZ)
        val packet = ClientboundBlockDestructionPacket(entityId, blockPos, -1)
        arena.playing.forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }

    private fun setBlockToAir() {
        val worldServer = (location.world as CraftWorld).handle
        val blockPos = BlockPos(location.blockX, location.blockY, location.blockZ)
        worldServer.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState())
        val packet = ClientboundBlockUpdatePacket(worldServer, blockPos)
        arena.playing.forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }
}