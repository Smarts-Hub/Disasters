package me.hhitt.disasters.storage.file

import me.hhitt.disasters.Disasters
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

/**
 * Configuration class for loading and saving YAML configuration files.
 *
 * @param file The file to load the configuration from. If null, the file will be created in the plugin's data folder.
 * @param fileName The name of the configuration file. Should not be null.
 */

open class Configuration(file: File?, fileName: String) : YamlConfiguration() {

    private val file: File

    init {
        requireNotNull(fileName) { "File name cannot be null" }
        this.file = if (file != null && file.isDirectory) {
            File(file, if (fileName.endsWith(".yml")) fileName else "$fileName.yml")
        } else {
            File(Disasters.getInstance().dataFolder, if (fileName.endsWith(".yml")) fileName else "$fileName.yml")
        }
        saveDefault()
        loadFile()
    }

    private fun loadFile() {
        try {
            this.load(file)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
        }
    }

    private fun saveDefault() {
        if (!file.exists()) {
            Disasters.getInstance().saveResource(file.name, false)
        }
    }

    fun save() {
        try {
            this.save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun reloadFile() {
        try {
            loadFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
