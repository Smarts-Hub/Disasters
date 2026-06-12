package me.hhitt.disasters.listener

import me.hhitt.disasters.arena.ArenaManager
import me.hhitt.disasters.model.arena.JumpPad
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class JumpPadListener(private val arenaManager: ArenaManager) : Listener {

    private data class CooldownKey(val player: UUID, val padId: String)
    private val cooldowns = ConcurrentHashMap<CooldownKey, Long>()

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.from.block == event.to.block) return
        val arena = arenaManager.getArena(event.player) ?: return
        if (arena.jumpPads.isEmpty()) return
        val player = event.player
        val to = event.to
        val key = CooldownKey(player.uniqueId, "")
        val now = System.currentTimeMillis()
        val pad = findPad(arena.jumpPads, to.world.name, to.blockX, to.blockY - 1, to.blockZ) ?: return
        val cooldownKey = CooldownKey(player.uniqueId, pad.id)
        val next = cooldowns[cooldownKey] ?: 0L
        if (now < next) return
        cooldowns[cooldownKey] = now + pad.cooldownTicks * 50L

        val dir = player.location.direction
        val v = Vector(dir.x, 0.0, dir.z)
        if (v.lengthSquared() > 0.0) v.normalize().multiply(pad.powerForward)
        v.y = pad.powerY
        player.velocity = v
        player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f)
    }

    private fun findPad(pads: List<JumpPad>, world: String, x: Int, y: Int, z: Int): JumpPad? {
        for (pad in pads) {
            if (pad.location.world?.name != world) continue
            if (pad.location.blockX == x && pad.location.blockY == y && pad.location.blockZ == z) {
                return pad
            }
        }
        return null
    }
}
