package me.hhitt.disasters.disaster.impl

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.arena.Arena
import me.hhitt.disasters.disaster.Disaster
import me.hhitt.disasters.util.Notify
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.FallingBlock
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MeteorShower : Disaster {

    private val arenas = mutableListOf<Arena>()
    private val activeMeteors = mutableMapOf<Arena, MutableList<MeteorTracker>>()
    private val plugin = Disasters.getInstance()

    // Duration in seconds — disaster stops spawning new meteors after this
    private val duration = 180
    private var elapsed = 0
    private var active = true

    private data class MeteorTracker(
        val entity: FallingBlock,
        val target: Location,
        var ticks: Int = 0,
        var lastY: Double = Double.MAX_VALUE
    )

    override fun start(arena: Arena) {
        arenas.add(arena)
        activeMeteors[arena] = mutableListOf()
        Notify.disaster(arena, "meteor-shower")
    }

    override fun pulse(time: Int) {
        elapsed++
        // Stop spawning new meteors after duration, but let existing ones finish
        if (elapsed >= duration) {
            active = false
        }

        arenas.toList().forEach { arena ->
            val meteors = activeMeteors[arena] ?: return@forEach

            // --- SPAWN METEORS ---
            if (active) {
                arena.alive.forEach { player ->
                    if (Random.nextInt(3) != 0) return@forEach

                    val world = player.world

                    // target near player, slightly ahead of movement
                    val target = player.location.clone().add(
                        Random.nextDouble(-8.0, 8.0) + player.velocity.x * 5,
                        0.0,
                        Random.nextDouble(-8.0, 8.0) + player.velocity.z * 5
                    )
                    target.y = world.getHighestBlockYAt(target.blockX, target.blockZ).toDouble() + 1

                    // spawn high above
                    val spawn = target.clone().add(
                        Random.nextDouble(-3.0, 3.0),
                        Random.nextInt(20, 30).toDouble(),
                        Random.nextDouble(-3.0, 3.0)
                    )

                    val meteor = world.spawn(spawn, FallingBlock::class.java)
                    meteor.blockData = Material.MAGMA_BLOCK.createBlockData()
                    meteor.dropItem = false
                    meteor.setHurtEntities(false) // we handle damage manually
                    meteor.isSilent = true

                    // velocity toward target
                    val direction = target.toVector()
                        .subtract(spawn.toVector())
                        .normalize()
                        .multiply(1.8)
                    meteor.velocity = direction

                    // red warning circle on ground at target
                    for (i in 0..12) {
                        val angle = i * (Math.PI * 2 / 12)
                        val px = target.x + cos(angle) * 2.0
                        val pz = target.z + sin(angle) * 2.0
                        world.spawnParticle(
                            Particle.DUST,
                            px, target.y + 0.2, pz,
                            2, 0.1, 0.0, 0.1,
                            Particle.DustOptions(org.bukkit.Color.RED, 1.5f)
                        )
                    }

                    world.playSound(spawn, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.4f)

                    meteors.add(MeteorTracker(meteor, target))
                }
            }

            // --- TRACK METEORS ---
            val iter = meteors.iterator()
            while (iter.hasNext()) {
                val m = iter.next()
                m.ticks++

                if (m.entity.isDead) {
                    // entity died (probably hit something) — create impact where it died
                    createImpact(arena, m.entity.location)

                    iter.remove()
                    continue
                }

                val loc = m.entity.location
                val world = loc.world

                // trail particles
                world.spawnParticle(Particle.FLAME, loc, 6, 0.2, 0.2, 0.2, 0.02)
                world.spawnParticle(Particle.SMOKE, loc, 4, 0.15, 0.15, 0.15, 0.01)
                world.spawnParticle(Particle.LAVA, loc, 1, 0.1, 0.1, 0.1)

                // detect impact: block stopped moving (Y not changing) or went below ground
                val groundY = world.getHighestBlockYAt(loc.blockX, loc.blockZ).toDouble()
                val stopped = Math.abs(loc.y - m.lastY) < 0.05 && m.ticks > 5
                val belowGround = loc.y <= groundY + 1.0 && m.ticks > 5
                val timedOut = m.ticks > 80

                m.lastY = loc.y

                if (stopped || belowGround || timedOut) {
                    createImpact(arena, loc)
                    m.entity.remove()
                    iter.remove()
                }
            }
        }
    }

    private fun createImpact(arena: Arena, location: Location) {
        val world = location.world

        // --- PARTICLES ---
        world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 2)
        world.spawnParticle(Particle.FLAME, location, 40, 2.5, 1.5, 2.5, 0.1)
        world.spawnParticle(Particle.LAVA, location, 20, 2.0, 0.5, 2.0)
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 15, 1.5, 1.5, 1.5, 0.05)

        // --- SOUNDS ---
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 4f, 0.5f)
        world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 3f, 0.4f)
        // 💥 ACTUAL TERRAIN DAMAGE
        world.createExplosion(location, 4f, false, true)

        // --- DAMAGE + KNOCKBACK nearby players (manual, not relying on explosion) ---
        arena.alive.forEach { player ->
            val dist = player.location.distance(location)
            if (dist < 7.0) {
                // damage scales with distance: 8 at center, less further out
                val damage = (8.0 * (1.0 - dist / 7.0)).coerceAtLeast(1.0)
                player.damage(damage)

                // knockback away from impact
                val knockback = player.location.toVector()
                    .subtract(location.toVector())
                    .normalize()
                    .multiply(1.5 * (1.0 - dist / 7.0))
                knockback.y = 0.5
                player.velocity = player.velocity.add(knockback)
            }
        }

        // --- FIRE at impact ---
        val fireBlocks = mutableListOf<Location>()
        for (dx in -2..2) {
            for (dz in -2..2) {
                if (Random.nextInt(3) == 0) continue
                val fireY = world.getHighestBlockYAt(
                    location.blockX + dx, location.blockZ + dz
                )
                val fireLoc = Location(world,
                    (location.blockX + dx).toDouble(),
                    fireY + 1.0,
                    (location.blockZ + dz).toDouble()
                )
                if (fireLoc.block.type == Material.AIR) {
                    fireLoc.block.type = Material.FIRE
                    fireBlocks.add(fireLoc)
                }
            }
        }

        // clean up fire after 3 seconds
        object : BukkitRunnable() {
            override fun run() {
                fireBlocks.forEach { if (it.block.type == Material.FIRE) it.block.type = Material.AIR }
            }
        }.runTaskLater(plugin, 60L)

        // --- DEBRIS FallingBlocks flung outward ---
        for (i in 0..6) {
            val debrisMat = listOf(
                Material.COBBLESTONE, Material.NETHERRACK,
                Material.MAGMA_BLOCK, Material.BLACKSTONE
            ).random()

            val debris = world.spawn(
                location.clone().add(0.0, 2.0, 0.0),
                FallingBlock::class.java
            )
            debris.blockData = debrisMat.createBlockData()
            debris.dropItem = false
            debris.setHurtEntities(true)
            debris.setDamagePerBlock(2f)
            debris.setMaxDamage(6)
            debris.velocity = Vector(
                Random.nextDouble(-0.8, 0.8),
                Random.nextDouble(0.5, 1.2),
                Random.nextDouble(-0.8, 0.8)
            )
        }
    }

    override fun stop(arena: Arena) {
        activeMeteors[arena]?.forEach { if (!it.entity.isDead) it.entity.remove() }
        activeMeteors.remove(arena)
        arenas.remove(arena)
    }
}