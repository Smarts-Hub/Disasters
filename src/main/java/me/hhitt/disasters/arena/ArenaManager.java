package me.hhitt.disasters.arena;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.model.arena.JumpPad;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

public class ArenaManager {

    private final WorldEditPlugin worldEdit;
    private final Disasters plugin;
    private final List<Arena> arenas = new ArrayList<>();

    public ArenaManager(final WorldEditPlugin worldEdit) {
        this.worldEdit = worldEdit;
        this.plugin = Disasters.getInstance();
        createDefaultArenaFile();
        loadArenas();
    }

    private void createDefaultArenaFile() {
        final File arenasFolder = new File(plugin.getDataFolder(), "Arenas");
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }

        final File arenaFile = new File(arenasFolder, "example_arena.yml");
        if (!arenaFile.exists()) {
            final InputStream inputStream = plugin.getResource("example_arena.yml");
            if (inputStream != null) {
                try (InputStream autoClose = inputStream) {
                    Files.copy(autoClose, arenaFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (final Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to copy default arena file", e);
                }
            }
        }
    }

    private List<JumpPad> loadJumpPads(final YamlConfiguration arenaConfig) {
        final ConfigurationSection section = arenaConfig.getConfigurationSection("jump-pads");
        if (section == null) {
            return Collections.emptyList();
        }
        final List<JumpPad> jumpPads = new ArrayList<>();
        for (final String id : section.getKeys(false)) {
            final ConfigurationSection pad = section.getConfigurationSection(id);
            if (pad == null) continue;
            final String worldName = pad.getString("world");
            if (worldName == null) continue;
            final World world = Bukkit.getWorld(worldName);
            if (world == null) continue;
            jumpPads.add(new JumpPad(
                id,
                new Location(world, pad.getDouble("x"), pad.getDouble("y"), pad.getDouble("z")),
                pad.getDouble("power-y", 1.1),
                pad.getDouble("power-forward", 0.8),
                pad.getLong("cooldown-ticks", 20L)
            ));
        }
        return jumpPads;
    }

    private void loadArenas() {
        final File arenasFolder = new File(plugin.getDataFolder(), "Arenas");

        if (!arenasFolder.exists() || !arenasFolder.isDirectory()) {
            plugin.getLogger().severe("Arenas folder does not exist or is not a directory.");
            return;
        }

        final File[] arenaFiles = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (arenaFiles == null || arenaFiles.length == 0) {
            plugin.getLogger().warning("No arena files found in the Arenas folder.");
            return;
        }

        for (final File arenaFile : arenaFiles) {
            final YamlConfiguration arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
            final String arenaID = arenaFile.getName().replace(".yml", "");
            final int gameTime = arenaConfig.getInt("game-time");
            final int countdown = arenaConfig.getInt("countdown");
            final int maxPlayers = arenaConfig.getInt("max-players");
            final int minPlayers = arenaConfig.getInt("min-players");
            final int aliveToEnd = arenaConfig.getInt("alive-to-end");
            final int disasterRate = arenaConfig.getInt("disaster-rate");
            final int maxDisasters = arenaConfig.getInt("max-disasters");
            final String displayName = arenaConfig.getString("display-name");
            if (displayName == null) {
                plugin.getLogger().warning("Arena file " + arenaFile.getName() + " is missing display-name, skipping.");
                continue;
            }

            final String spawnWorldName = arenaConfig.getString("spawn.world");
            if (spawnWorldName == null) {
                plugin.getLogger().warning("Arena file " + arenaFile.getName() + " is missing spawn.world, skipping.");
                continue;
            }
            final World spawnWorld = Bukkit.getWorld(spawnWorldName);
            if (spawnWorld == null) {
                plugin.getLogger().warning("Arena file " + arenaFile.getName() + " has invalid spawn world, skipping.");
                continue;
            }
            final Location location = new Location(
                spawnWorld,
                arenaConfig.getDouble("spawn.x"),
                arenaConfig.getDouble("spawn.y"),
                arenaConfig.getDouble("spawn.z"),
                (float) arenaConfig.getInt("spawn.yaw"),
                (float) arenaConfig.getInt("spawn.pitch")
            );

            final List<Location> spawns = new ArrayList<>();
            if (arenaConfig.contains("spawns")) {
                final ConfigurationSection spawnsSection = arenaConfig.getConfigurationSection("spawns");
                if (spawnsSection != null) {
                    for (final String key : spawnsSection.getKeys(false)) {
                        final ConfigurationSection s = spawnsSection.getConfigurationSection(key);
                        if (s == null) continue;
                        final String sWorldName = s.getString("world");
                        if (sWorldName == null) continue;
                        final World sWorld = Bukkit.getWorld(sWorldName);
                        if (sWorld == null) continue;
                        spawns.add(new Location(
                            sWorld,
                            s.getDouble("x"),
                            s.getDouble("y"),
                            s.getDouble("z"),
                            (float) s.getInt("yaw"),
                            (float) s.getInt("pitch")
                        ));
                    }
                }
            }

            final String c1WorldName = arenaConfig.getString("corner1.world");
            final String c2WorldName = arenaConfig.getString("corner2.world");
            if (c1WorldName == null || c2WorldName == null) {
                plugin.getLogger().warning("Arena file " + arenaFile.getName() + " is missing corner world, skipping.");
                continue;
            }
            final World c1World = Bukkit.getWorld(c1WorldName);
            final World c2World = Bukkit.getWorld(c2WorldName);
            if (c1World == null || c2World == null) {
                plugin.getLogger().warning("Arena file " + arenaFile.getName() + " has invalid corner world, skipping.");
                continue;
            }
            final Location corner1 = new Location(
                c1World,
                arenaConfig.getDouble("corner1.x"),
                arenaConfig.getDouble("corner1.y"),
                arenaConfig.getDouble("corner1.z")
            );
            final Location corner2 = new Location(
                c2World,
                arenaConfig.getDouble("corner2.x"),
                arenaConfig.getDouble("corner2.y"),
                arenaConfig.getDouble("corner2.z")
            );

            final List<String> winnersCommands = arenaConfig.getStringList("winners-commands");
            final List<String> losersCommands = arenaConfig.getStringList("losers-commands");
            final List<String> toAllCommands = arenaConfig.getStringList("to-all-commands");

            final List<JumpPad> jumpPads = loadJumpPads(arenaConfig);
            final Arena arena = new Arena(arenaID, displayName, minPlayers, maxPlayers, aliveToEnd, gameTime, countdown,
                disasterRate, maxDisasters, location, spawns, jumpPads, corner1, corner2, winnersCommands, losersCommands,
                toAllCommands, worldEdit);

            arenas.add(arena);
        }
    }

    public List<Arena> getArenas() {
        return arenas;
    }

    public Arena getArena(final Player player) {
        for (final Arena arena : arenas) {
            if (arena.isPlayerValid(player)) {
                return arena;
            }
        }
        return null;
    }

    public Arena getArena(final String arenaId) {
        for (final Arena arena : arenas) {
            if (arena.getName().equalsIgnoreCase(arenaId)) {
                return arena;
            }
        }
        return null;
    }

    public boolean addPlayerToBestArena(final Player player) {
        final Arena bestArena = arenas.stream()
            .filter(arena -> arena.isWaiting() && !arena.isFull())
            .sorted(Comparator.<Arena, Integer>comparing(arena -> arena.getPlaying().size(), Comparator.reverseOrder())
                .thenComparing(Arena::getName))
            .findFirst()
            .orElse(null);
        if (bestArena != null) {
            return bestArena.addPlayer(player);
        }
        return false;
    }

    public void reloadArena(final File arenaFile) {
        final String arenaID = arenaFile.getName().replace(".yml", "");
        final Arena arena = getArena(arenaID);
        if (arena != null) {
            loadArenas();
        }
    }

    public boolean removeArena(final String arenaID) {
        final Arena arena = getArena(arenaID);
        if (arena == null) {
            return false;
        }
        return arenas.remove(arena);
    }

    public boolean reloadArenas() {
        for (final Arena arena : arenas) {
            if (!arena.isEmpty() || arena.getState() != me.hhitt.disasters.game.GameState.RECRUITING) {
                return false;
            }
        }
        arenas.clear();
        loadArenas();
        return true;
    }

    public Arena getArena(final Location location) {
        for (final Arena arena : arenas) {
            if (arena.getBorderService().isLocationInArena(location)) {
                return arena;
            }
        }
        return null;
    }
}
