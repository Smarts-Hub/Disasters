package me.hhitt.disasters.storage.file;

import me.hhitt.disasters.Disasters;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Configuration extends YamlConfiguration {

    private final File file;

    public Configuration(final File file, final String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }
        if (file != null && file.isDirectory()) {
            final String name = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
            this.file = new File(file, name);
        } else {
            final String name = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
            this.file = new File(Disasters.getInstance().getDataFolder(), name);
        }
        saveDefault();
        loadFile();
    }

    public Configuration(final String fileName) {
        this(null, fileName);
    }

    private void loadFile() {
        try {
            load(file);
        } catch (final IOException | InvalidConfigurationException e) {
            Disasters.getInstance().getLogger().log(Level.SEVERE, "Failed to load configuration: " + file.getName(), e);
        }
    }

    private void saveDefault() {
        if (!file.exists()) {
            Disasters.getInstance().saveResource(file.getName(), false);
        }
    }

    public void save() {
        try {
            save(file);
        } catch (final IOException e) {
            Disasters.getInstance().getLogger().log(Level.SEVERE, "Failed to save configuration: " + file.getName(), e);
        }
    }

    public void reloadFile() {
        loadFile();
    }
}
