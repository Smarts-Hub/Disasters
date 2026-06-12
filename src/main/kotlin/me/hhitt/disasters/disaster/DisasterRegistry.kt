package me.hhitt.disasters.disaster

import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.impl.AcidRain
import me.hhitt.disasters.disaster.impl.AnvilRain
import me.hhitt.disasters.disaster.impl.Apocalypse
import me.hhitt.disasters.disaster.impl.BatSwarm
import me.hhitt.disasters.disaster.impl.Blind
import me.hhitt.disasters.disaster.impl.BlockDisappear
import me.hhitt.disasters.disaster.impl.Cobweb
import me.hhitt.disasters.disaster.impl.Covid19
import me.hhitt.disasters.disaster.impl.Disco
import me.hhitt.disasters.disaster.impl.ExplosiveSheep
import me.hhitt.disasters.disaster.impl.FloorIsLava
import me.hhitt.disasters.disaster.impl.Flood
import me.hhitt.disasters.disaster.impl.Freeze
import me.hhitt.disasters.disaster.impl.Grounded
import me.hhitt.disasters.disaster.impl.HotPotato
import me.hhitt.disasters.disaster.impl.HotSun
import me.hhitt.disasters.disaster.impl.Lag
import me.hhitt.disasters.disaster.impl.Landmine
import me.hhitt.disasters.disaster.impl.LavaRising
import me.hhitt.disasters.disaster.impl.Lightning
import me.hhitt.disasters.disaster.impl.MeteorShower
import me.hhitt.disasters.disaster.impl.MirrorControls
import me.hhitt.disasters.disaster.impl.Nuke
import me.hhitt.disasters.disaster.impl.PillagerInvasion
import me.hhitt.disasters.disaster.impl.RedLightGreenLight
import me.hhitt.disasters.disaster.impl.SimonSays
import me.hhitt.disasters.disaster.impl.Sinkhole
import me.hhitt.disasters.disaster.impl.SizeChange
import me.hhitt.disasters.disaster.impl.TntRain
import me.hhitt.disasters.disaster.impl.Tornado
import me.hhitt.disasters.disaster.impl.Wither
import me.hhitt.disasters.disaster.impl.WorldBorder
import me.hhitt.disasters.disaster.impl.ZeroGravity
import me.hhitt.disasters.model.block.DisappearBlock
import me.hhitt.disasters.model.block.DisasterFloor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object DisasterRegistry {

    private val activeDisasters = ConcurrentHashMap<Arena, CopyOnWriteArrayList<ActiveDisaster>>()

    private val definitions: List<DisasterDefinition> = listOf(
        DisasterDefinition("acid-rain", "Acid Rain", AcidRain::class, factory = { AcidRain() }),
        DisasterDefinition("apocalypse", "Zombie Apocalypse", Apocalypse::class, factory = { Apocalypse() }),
        DisasterDefinition("explosive-sheep", "Explosive Sheep", ExplosiveSheep::class, factory = { ExplosiveSheep() }),
        DisasterDefinition("floor-is-lava", "Floor Is Lava", FloorIsLava::class, factory = { FloorIsLava() }),
        DisasterDefinition("grounded", "Grounded", Grounded::class, factory = { Grounded() }),
        DisasterDefinition("lightning", "Lightning", Lightning::class, factory = { Lightning() }),
        DisasterDefinition("world-border", "World Border", WorldBorder::class, factory = { WorldBorder() }),
        DisasterDefinition("blind", "Blind", Blind::class, factory = { Blind() }),
        DisasterDefinition("cobweb", "Cobweb", Cobweb::class, factory = { Cobweb() }),
        DisasterDefinition("lag", "Lag", Lag::class, factory = { Lag() }),
        DisasterDefinition("zero-gravity", "Zero Gravity", ZeroGravity::class, factory = { ZeroGravity() }),
        DisasterDefinition("wither", "Wither", Wither::class, factory = { Wither() }),
        DisasterDefinition("hot-sun", "Hot Sun", HotSun::class, factory = { HotSun() }),
        DisasterDefinition("disappear-blocks", "Disappear Blocks", BlockDisappear::class, factory = { BlockDisappear() }),
        DisasterDefinition("meteor-shower", "Meteor Shower", MeteorShower::class, factory = { MeteorShower() }),
        DisasterDefinition("flood", "Flood", Flood::class, factory = { Flood() }),
        DisasterDefinition("lava-rising", "Lava Rising", LavaRising::class, factory = { LavaRising() }, incompatibleWith = setOf("flood")),
        DisasterDefinition("tnt-rain", "TNT Rain", TntRain::class, factory = { TntRain() }),
        DisasterDefinition("anvil-rain", "Anvil Rain", AnvilRain::class, factory = { AnvilRain() }),
        DisasterDefinition("tornado", "Tornado", Tornado::class, factory = { Tornado() }),
        DisasterDefinition("simon-says", "Simon Says", SimonSays::class, factory = { SimonSays() }),
        DisasterDefinition("hot-potato", "Hot Potato", HotPotato::class, factory = { HotPotato() }),
        DisasterDefinition("sinkhole", "Sinkhole", Sinkhole::class, factory = { Sinkhole() }),
        DisasterDefinition("pillager-invasion", "Pillager Invasion", PillagerInvasion::class, factory = { PillagerInvasion() }),
        DisasterDefinition("freeze", "Freeze", Freeze::class, factory = { Freeze() }),
        DisasterDefinition("landmine", "Landmine", Landmine::class, factory = { Landmine() }),
        DisasterDefinition("size-change", "Size Change", SizeChange::class, factory = { SizeChange() }),
        DisasterDefinition("bat-swarm", "Bat Swarm", BatSwarm::class, factory = { BatSwarm() }),
        DisasterDefinition("nuke", "Nuke", Nuke::class, factory = { Nuke() }),
        DisasterDefinition("covid-19", "Covid-19", Covid19::class, factory = { Covid19() }),
        DisasterDefinition("disco", "Disco", Disco::class, factory = { Disco() }, incompatibleWith = setOf("red-light-green-light")),
        DisasterDefinition("red-light-green-light", "Red Light Green Light", RedLightGreenLight::class, factory = { RedLightGreenLight() }, incompatibleWith = setOf("disco")),
        DisasterDefinition("mirror-controls", "Mirror Controls", MirrorControls::class, factory = { MirrorControls() })
    )

    private val noDisasterWarned = mutableSetOf<Arena>()

    private inline fun <reified T : Disaster> getDisaster(arena: Arena): T? {
        return activeDisasters[arena]?.map { it.disaster }?.find { it is T } as? T
    }

    fun addRandomDisaster(arena: Arena) {
        val maxDisasters = DisasterSettings.maxSimultaneousDisasters(arena)
        val currentDisasters = activeDisasters.getOrPut(arena) { CopyOnWriteArrayList() }

        if (currentDisasters.size >= maxDisasters) {
            val toRemove = currentDisasters.removeAt(0)
            toRemove.disaster.stop(arena)
            arena.disasters.remove(toRemove.disaster)
        }

        val activeIds = currentDisasters.map { it.definition.id }.toSet()
        val activeIncompatibilities = currentDisasters.flatMap { it.definition.incompatibleWith }.toSet()
        val available = definitions.filter { def ->
            def.id !in activeIds &&
                DisasterSettings.isDisasterEnabled(arena, def.id) &&
                def.incompatibleWith.none { it in activeIds } &&
                def.id !in activeIncompatibilities
        }

        if (available.isEmpty()) {
            if (noDisasterWarned.add(arena)) {
                me.hhitt.disasters.Disasters.getInstance().logger.warning("No enabled compatible disaster for arena ${arena.name}.")
            }
            return
        }

        val definition = available.random()
        val disaster = definition.factory()
        disaster.start(arena)
        val active = ActiveDisaster(
            definition = definition,
            disaster = disaster,
            elapsedSeconds = 0,
            durationSeconds = DisasterSettings.durationSeconds(definition.id),
            maxTriggers = DisasterSettings.maxTriggers(definition.id)
        )
        currentDisasters.add(active)
        arena.disasters.add(disaster)
    }

    fun pulseAll(time: Int) {
        activeDisasters.forEach { (arena, list) ->
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val wrapper = iterator.next()
                wrapper.disaster.pulse(time)
                wrapper.elapsedSeconds++

                val triggerCap = wrapper.maxTriggers
                val triggered = wrapper.disaster is TriggerTrackedDisaster &&
                    (wrapper.disaster as TriggerTrackedDisaster).triggerCount >= triggerCap && triggerCap > 0
                val durationReached = wrapper.elapsedSeconds >= wrapper.durationSeconds

                if (durationReached || triggered) {
                    wrapper.disaster.stop(arena)
                    arena.disasters.remove(wrapper.disaster)
                    iterator.remove()
                }
            }
        }
    }

    fun removeDisasters(arena: Arena) {
        activeDisasters.remove(arena)?.forEach { wrapper ->
            wrapper.disaster.stop(arena)
            arena.disasters.remove(wrapper.disaster)
        }
        arena.disasters.clear()
        noDisasterWarned.remove(arena)
    }

    fun getActiveDisasterNames(arena: Arena): List<String> {
        return activeDisasters[arena]?.map { it.definition.displayName } ?: emptyList()
    }

    fun hasDisaster(arena: Arena, id: String): Boolean {
        return activeDisasters[arena]?.any { it.definition.id == id } ?: false
    }

    private fun isPlayerOnClimbable(location: Location): Boolean {
        return Tag.CLIMBABLE.isTagged(location.block.type)
    }

    private fun getBlockUnderPlayer(location: Location): Location? {
        if (location.y % 1.0 != 0.0) {
            val atFeet = location.clone()
            if (!atFeet.block.type.isAir && atFeet.block.type.isSolid) {
                return atFeet
            }
        }
        val blockBelow = location.clone().subtract(0.0, 1.0, 0.0)
        if (!blockBelow.block.type.isAir) {
            return blockBelow
        }
        return null
    }

    fun addBlockToDisappear(arena: Arena, location: Location) {
        if (isPlayerOnClimbable(location)) return
        val block = getBlockUnderPlayer(location) ?: return
        getDisaster<BlockDisappear>(arena)?.addBlock(arena, block)
    }

    fun removeBlockFromDisappear(arena: Arena, block: DisappearBlock) {
        getDisaster<BlockDisappear>(arena)?.removeBlock(block)
    }

    fun setBlockUnoccupied(arena: Arena, location: Location) {
        getDisaster<BlockDisappear>(arena)?.setUnoccupied(location)
    }

    fun addBlockToFloorIsLava(arena: Arena, location: Location) {
        if (isPlayerOnClimbable(location)) return
        val block = getBlockUnderPlayer(location) ?: return
        val blockType = block.block.type
        if (blockType == Material.WATER || blockType == Material.LAVA) return
        val floorBlock = DisasterFloor(arena, block)
        getDisaster<FloorIsLava>(arena)?.addBlock(floorBlock)
    }

    fun removeBlockFromFloorIsLava(arena: Arena, block: DisasterFloor) {
        getDisaster<FloorIsLava>(arena)?.removeBlock(block)
    }

    fun isGrounded(arena: Arena, player: Player): Boolean {
        return getDisaster<Grounded>(arena)?.isGrounded(player) ?: false
    }
}
