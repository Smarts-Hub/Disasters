package me.hhitt.disasters.service

import me.hhitt.disasters.storage.file.FileManager
import me.hhitt.disasters.util.Msg
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object DeathMessageService {

    private data class Mark(val disasterId: String, val timestampMs: Long)

    private val marks = ConcurrentHashMap<UUID, Mark>()

    fun mark(player: Player, disasterId: String) {
        marks[player.uniqueId] = Mark(disasterId, System.currentTimeMillis())
    }

    fun messageFor(player: Player, fallbackDisasterId: String = "default"): Component {
        val now = System.currentTimeMillis()
        val mark = marks[player.uniqueId]
        val id = if (mark != null && now - mark.timestampMs <= 10_000) {
            mark.disasterId
        } else {
            fallbackDisasterId
        }
        val config = FileManager.get("deadmessages")
        val raw = config?.getString("messages.$id")
            ?: config?.getString("messages.default")
            ?: "${player.name} died."
        val resolved = raw.replace("%player%", player.name).replace("%disaster%", id)
        return Msg.parse(resolved, player)
    }

    fun clear(player: Player) {
        marks.remove(player.uniqueId)
    }
}
