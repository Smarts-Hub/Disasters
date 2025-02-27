package me.hhitt.disasters.util

import java.io.File

object Filer {

    fun fixName(name: String): String {
        return if (name.endsWith(".yml")) name else "$name.yml"
    }

    fun createFolders(){
        val arenasFolder = File("plugins/Disasters/Arenas")
        if (!arenasFolder.exists()) arenasFolder.mkdirs()

    }

}