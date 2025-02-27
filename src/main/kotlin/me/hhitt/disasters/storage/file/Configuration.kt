package me.hhitt.disasters.storage.file

import me.hhitt.disasters.Disasters
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.Objects.requireNonNull

open class Configuration(file: File?, fileName: String) : YamlConfiguration() {

    private var fileName : String
    private var file : File

    init {
        requireNonNull(fileName, "File name cannot be null")
        requireNonNull(file)
        this.fileName = if (fileName.endsWith(".yml")) fileName else "$fileName.yml"
        this.file = File(file, fileName)
        saveDef()
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

    private fun saveDef() {
        if (!file.exists()) {
            Disasters.getInstance().saveResource(fileName, false)
        }
    }

    fun save() {
        try {
            save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun reloadFile() {
        try {
            load(file)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
        }
    }

}