package me.hhitt.disasters.storage.file;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.util.Filer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class FileManager {

    private static final Map<String, Configuration> configs = new HashMap<>();

    private FileManager() {
    }

    public static void initialize() {
        updateConfigFile("config.yml");
        updateConfigFile("lang.yml");
        updateConfigFile("scoreboard.yml");
        updateConfigFile("enabledisasters.yml");
        updateConfigFile("deadmessages.yml");
        updateConfigFile("item-drops.yml");

        load("config");
        load("lang");
        load("scoreboard");
        load("enabledisasters");
        load("deadmessages");
        load("item-drops");
    }

    private static void load(final String name) {
        String fileName = Filer.fixName(name);
        File file = new File(Disasters.getInstance().getDataFolder(), fileName);
        if (!file.exists()) {
            Disasters.getInstance().saveResource(fileName, false);
        }
        Configuration config = new Configuration(file, fileName);
        configs.put(fileName, config);
    }

    private static void updateConfigFile(final String fileName) {
        try {
            File configFile = new File(Disasters.getInstance().getDataFolder(), fileName);

            if (!Disasters.getInstance().getDataFolder().exists()) {
                Disasters.getInstance().getDataFolder().mkdirs();
            }

            java.io.InputStream defaultResource = Disasters.getInstance().getResource(fileName);

            if (defaultResource == null) {
                if (!configFile.exists()) {
                    Disasters.getInstance().getLogger().warning("Cannot find default file for: " + fileName);
                }
                return;
            }

            if (configFile.exists() && !hasConfigVersion(configFile)) {
                Disasters.getInstance().getLogger().info("Adding config-version to: " + fileName);
                addConfigVersion(configFile);
            }

            YamlDocument config = YamlDocument.create(
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
                    .setVersioning(new BasicVersioning("config-version"))
                    .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                    .setKeepAll(true)
                    .build()
            );

            if (config.update()) {
                Disasters.getInstance().getLogger().info("Updated configuration: " + fileName);
                config.save();
            }

        } catch (IOException e) {
            Disasters.getInstance().getLogger().log(Level.SEVERE, "Error updating config: " + fileName, e);
        } catch (Exception e) {
            Disasters.getInstance().getLogger().log(Level.WARNING, "Error processing: " + fileName, e);
        }
    }

    private static boolean hasConfigVersion(final File file) {
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            return yaml.contains("config-version");
        } catch (Exception e) {
            return false;
        }
    }

    private static void addConfigVersion(final File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());

            if (!lines.isEmpty()) {
                int insertIndex = 0;

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.startsWith("#") || line.isEmpty()) {
                        insertIndex = i + 1;
                    } else {
                        break;
                    }
                }

                lines.add(insertIndex, "config-version: 1");
                if (insertIndex < lines.size() - 1 && !lines.get(insertIndex + 1).isEmpty()) {
                    lines.add(insertIndex + 1, "");
                }

                Files.write(file.toPath(), lines);
            } else {
                Files.write(file.toPath(), java.util.Collections.singletonList("config-version: 1"));
            }
        } catch (Exception e) {
            Disasters.getInstance().getLogger().log(Level.WARNING, "Failed to add config-version to: " + file.getName(), e);
        }
    }

    public static void reload(final String name) {
        String fileName = Filer.fixName(name);
        Configuration config = configs.get(fileName);
        if (config == null) {
            return;
        }

        updateConfigFile(fileName);
        config.reloadFile();
    }

    public static void save(final String name) {
        String fileName = Filer.fixName(name);
        Configuration config = configs.get(fileName);
        if (config == null) {
            return;
        }

        config.save();
    }

    public static Configuration get(final String name) {
        return configs.get(Filer.fixName(name));
    }
}
