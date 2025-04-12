package me.hhitt.disasters.storage.file

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.util.Filer.fixName
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * FileManager is responsible for managing configuration files in the plugin.
 * It loads, saves, and reloads configuration files as needed.
 *
 * @property configs A map of configuration file names to their corresponding Configuration objects.
 * @property plugin The instance of the JavaPlugin that this FileManager is associated with.
 */

object FileManager {

    private val configs = mutableMapOf<String, Configuration>()
    private lateinit var plugin: JavaPlugin

    fun initialize() {
        this.plugin = Disasters.getInstance()
        load("config")
        load("lang")
        load("scoreboard")
    }

    private fun load(name: String) {
        val fileName = fixName(name)
        val file = File(plugin.dataFolder, fileName)
        val config = Configuration(file, fileName)

        config.load(file)
        configs[fileName] = config
    }

    fun reload(name: String) {

        val fileName = fixName(name)

        val config = configs[fileName] ?: return
        config.reloadFile()
    }

    fun save(name: String) {

        val fileName = fixName(name)

        val config = configs[fileName] ?: return
        val file = File(plugin.dataFolder, fileName)
        config.save(file)
    }

    fun get(name: String): Configuration? {
        return configs[fixName(name)]
    }

    fun getArenaConfig(name: String): Configuration? {
        val file = File("plugins/Disasters/Arenas", fixName(name))
        return Configuration(file, name)
    }

}
