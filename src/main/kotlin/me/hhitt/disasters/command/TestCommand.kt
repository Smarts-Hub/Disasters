package me.hhitt.disasters.command

import me.hhitt.disasters.obj.entity.DisasterSheep
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.CraftWorld
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.actor.BukkitCommandActor

@Command("test")
class TestCommand {


    @Subcommand("sheep")
    fun sheep(actor: BukkitCommandActor){

        val player = actor.asPlayer()
        val worldServer: ServerLevel = (player!!.location.world as CraftWorld).handle


        repeat(10000){
            val sheep = DisasterSheep(EntityType.SHEEP, (player!!.location.world as CraftWorld).handle.level, player.location)
            worldServer.addFreshEntity(sheep)
        }

    }

}