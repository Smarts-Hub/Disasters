package me.hhitt.disasters.command

import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand

@Command("disasters")
class DisastersCommand {

    @Subcommand("reload")
    fun reload() {
    }
}