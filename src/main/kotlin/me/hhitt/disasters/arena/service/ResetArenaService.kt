package me.hhitt.disasters.arena.service

import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.game.GameState
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import kotlin.math.max
import kotlin.math.min

/**
 * ResetArenaService is responsible for resetting the arena to its original state.
 * It saves the current state of the arena and pastes it back when needed.
 *
 * @param arena The arena to be reset.
 * @param worldEdit The WorldEdit plugin instance, if available.
 */

class ResetArenaService(
    private val arena: Arena,
    private val worldEdit: WorldEditPlugin?) {

    private val world = arena.corner1.world
    private lateinit var clipboard: Clipboard
    private lateinit var center: BlockVector3
    private var saveFuture: CompletableFuture<Void>? = null

    private val minX = min(arena.corner1.x, arena.corner2.x).toInt()
    private val maxX = max(arena.corner1.x, arena.corner2.x).toInt()
    private val minY = min(arena.corner1.y, arena.corner2.y).toInt()
    private val maxY = max(arena.corner1.y, arena.corner2.y).toInt()
    private val minZ = min(arena.corner1.z, arena.corner2.z).toInt()
    private val maxZ = max(arena.corner1.z, arena.corner2.z).toInt()


    fun save() {
        val world = arena.corner1.world ?: return println("[Disasters] ERROR: World is null before save!")
        val min: BlockVector3 = BlockVector3.at(arena.corner1.x, arena.corner1.y, arena.corner1.z)
        val max: BlockVector3 = BlockVector3.at(arena.corner2.x, arena.corner2.y, arena.corner2.z)
        val region = CuboidRegion(min, max)
        val blockCount = region.volume
        println("[Disasters] Saving arena '${arena.name}' blocks: $blockCount (async)")

        saveFuture = CompletableFuture.runAsync {
            val clipboard = BlockArrayClipboard(region)
            worldEdit!!.worldEdit.newEditSession(BukkitAdapter.adapt(world)).use { editSession ->
                val forwardExtentCopy = ForwardExtentCopy(editSession, region, clipboard, region.minimumPoint)
                try {
                    Operations.complete(forwardExtentCopy)
                } catch (e: WorldEditException) {
                    throw RuntimeException(e)
                }
                this.clipboard = clipboard
                this.center = region.minimumPoint
            }
            println("[Disasters] Save complete for '${arena.name}'")
        }
    }

    fun waitForSave() {
        saveFuture?.let { future ->
            if (!future.isDone) {
                println("[Disasters] Waiting for save to complete for '${arena.name}'...")
                future.join()
                println("[Disasters] Save complete, starting game for '${arena.name}'")
            }
        }
    }

    fun paste() {
        // Ensure save is complete before pasting
        waitForSave()

        if (!::clipboard.isInitialized) {
            println("[Disasters] ERROR: Clipboard not initialized!")
            return
        }
        println("[Disasters] Paste triggered for '${arena.name}'")
        removeEntitiesInRegion()

        worldEdit!!.worldEdit.newEditSession(BukkitAdapter.adapt(world)).use { editSession ->
            println("[Disasters] Pasting arena '${arena.name}' now...")
            val operation = ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(center)
                .ignoreAirBlocks(false)
                .build()
            try {
                Operations.complete(operation)
            } catch (e: WorldEditException) {
                throw RuntimeException(e)
            }
        }
        refreshChunks(world, arena.corner1, arena.corner2)
        arena.state = GameState.RECRUITING
        println("[Disasters] Paste complete for '${arena.name}'")
    }

    private fun refreshChunks(world: World, loc1: Location, loc2: Location) {
        val minXX = minX shr 4
        val maxXX = maxX shr 4
        val minZZ = minZ shr 4
        val maxZZ = maxZ shr 4

        for (x in minXX..maxXX) {
            for (z in minZZ..maxZZ) {
                world.refreshChunk(x, z)
            }
        }
    }

    private fun removeEntitiesInRegion() {
        for (entity in world.entities) {
            val loc = entity.location
            if (loc.blockX in minX..maxX && loc.blockY in minY..maxY && loc.blockZ in minZ..maxZ) {
                if (entity !is Player) {
                    entity.remove()
                }
            }
        }
    }


}