package me.hhitt.disasters.util

import java.io.File

object Filer {

    // Make sure that the file name ends with .yml
    fun fixName(name: String): String {
        return if (name.endsWith(".yml")) name else "$name.yml"
    }

    // Create the folders needed for the plugin
    fun createFolders(){
        val arenasFolder = File("plugins/Disasters/Arenas")
        if (!arenasFolder.exists()) arenasFolder.mkdirs()

    }

}