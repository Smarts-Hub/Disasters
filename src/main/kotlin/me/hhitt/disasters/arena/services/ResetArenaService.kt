package me.hhitt.disasters.arena.services

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
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

class ResetArenaService(private val loc1: Location, private val loc2: Location) {

    private val world = loc1.world
    private lateinit var worldEdit: WorldEditPlugin
    private lateinit var clipboard: Clipboard
    private lateinit var center: BlockVector3

    private val minX = min(loc1.x, loc2.x)
    private val maxX = max(loc1.x, loc2.x)
    private val minY = min(loc1.y, loc2.y)
    private val maxY = max(loc1.y, loc2.y)
    private val minZ = min(loc1.z, loc2.z)
    private val maxZ = max(loc1.z, loc2.z)

    fun initWorldEdit(worldEdit: WorldEditPlugin) {
        this.worldEdit = worldEdit
    }

    fun save() {
        val min: BlockVector3 = BlockVector3.at(loc1.x, loc1.y, loc1.z)
        val max: BlockVector3 = BlockVector3.at(loc2.x, loc2.y, loc2.z)
        val region = CuboidRegion(min, max)
        val clipboard = BlockArrayClipboard(region)
        worldEdit.worldEdit.newEditSession(BukkitAdapter.adapt(world)).use { editSession ->
            val forwardExtentCopy = ForwardExtentCopy(editSession, region, clipboard, region.minimumPoint)
            try {
                Operations.complete(forwardExtentCopy)
            } catch (e: WorldEditException) {
                throw RuntimeException(e)
            }
            this.clipboard = clipboard
            this.center = region.minimumPoint
        }
    }

    fun paste() {

        for (entity in world.entities) {
            if (isEntityInRegion(entity)) {
                if (entity is Item || entity !is Player) {
                    entity.remove()
                }
            }
        }

        worldEdit.worldEdit.newEditSession(BukkitAdapter.adapt(world)).use { editSession ->
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
        refreshChunks(world, loc1, loc2)
    }

    private fun isEntityInRegion(entity: Entity): Boolean {
        val x = entity.location.x
        val y = entity.location.y
        val z = entity.location.z

        return (x in minX..maxX) && (y in minY..maxY) && (z in minZ..maxZ)
    }

    private fun refreshChunks(world: World, loc1: Location, loc2: Location) {
        val minX = min(loc1.blockX.toDouble(), loc2.blockX.toDouble()).toInt() shr 4
        val maxX = max(loc1.blockX.toDouble(), loc2.blockX.toDouble()).toInt() shr 4
        val minZ = min(loc1.blockZ.toDouble(), loc2.blockZ.toDouble()).toInt() shr 4
        val maxZ = max(loc1.blockZ.toDouble(), loc2.blockZ.toDouble()).toInt() shr 4

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                world.refreshChunk(x, z)
            }
        }
    }

}