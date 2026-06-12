package me.hhitt.disasters.storage.file

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import me.hhitt.disasters.Disasters
import me.hhitt.disasters.util.Filer.fixName
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.logging.Level

object FileManager {

    private val configs = mutableMapOf<String, Configuration>()
    private lateinit var plugin: JavaPlugin

    fun initialize() {
        this.plugin = Disasters.getInstance()

        updateConfigFile("config.yml")
        updateConfigFile("lang.yml")
        updateConfigFile("scoreboard.yml")
        updateConfigFile("enabledisasters.yml")
        updateConfigFile("deadmessages.yml")
        updateConfigFile("item-drops.yml")

        load("config")
        load("lang")
        load("scoreboard")
        load("enabledisasters")
        load("deadmessages")
    }

    private fun load(name: String) {
        val fileName = fixName(name)
        val file = File(plugin.dataFolder, fileName)
        val config = Configuration(file, fileName)

        config.load(file)
        configs[fileName] = config
    }

    private fun updateConfigFile(fileName: String) {
        try {
            val configFile = File(plugin.dataFolder, fileName)

            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }

            val defaultResource = plugin.getResource(fileName)

            if (defaultResource == null) {
                if (!configFile.exists()) {
                    plugin.logger.warning("Cannot find default file for: $fileName")
                }
                return
            }

            if (configFile.exists() && !hasConfigVersion(configFile)) {
                plugin.logger.info("Adding config-version to: $fileName")
                addConfigVersion(configFile)
            }

            val config = YamlDocument.create(
                configFile,
                defaultResource,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder()
                    .setAutoUpdate(true)
                    .build(),
                DumperSettings.builder()
                    .setEncoding(DumperSettings.Encoding.UNICODE)
                    .build(),
                UpdaterSettings.builder()
                    .setVersioning(BasicVersioning("config-version"))
                    .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                    .setKeepAll(true)
                    .build()
            )

            if (config.update()) {
                plugin.logger.info("Updated configuration: $fileName")
                config.save()
            }

        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Error updating config: $fileName", e)
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Error processing: $fileName", e)
        }
    }

    private fun hasConfigVersion(file: File): Boolean {
        return try {
            val yaml = YamlConfiguration.loadConfiguration(file)
            yaml.contains("config-version")
        } catch (e: Exception) {
            false
        }
    }

    private fun addConfigVersion(file: File) {
        try {
            val lines = file.readLines().toMutableList()

            if (lines.isNotEmpty()) {
                var insertIndex = 0

                for (i in lines.indices) {
                    val line = lines[i].trim()
                    if (line.startsWith("#") || line.isEmpty()) {
                        insertIndex = i + 1
                    } else {
                        break
                    }
                }

                lines.add(insertIndex, "config-version: 1")
                if (insertIndex < lines.size - 1 && lines[insertIndex + 1].isNotEmpty()) {
                    lines.add(insertIndex + 1, "")
                }

                file.writeText(lines.joinToString("\n"))
            } else {
                file.writeText("config-version: 1\n")
            }
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to add config-version to: ${file.name}", e)
        }
    }

    fun reload(name: String) {
        val fileName = fixName(name)
        val config = configs[fileName] ?: return

        updateConfigFile(fileName)
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