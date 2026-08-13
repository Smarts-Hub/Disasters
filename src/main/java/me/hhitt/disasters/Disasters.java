package me.hhitt.disasters;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.command.ArenaCommand;
import me.hhitt.disasters.command.DisastersCommand;
import me.hhitt.disasters.disaster.DisasterTask;
import me.hhitt.disasters.game.FinishReason;
import me.hhitt.disasters.game.drop.ItemDropManager;
import me.hhitt.disasters.game.modification.GameModificationDefinition;
import me.hhitt.disasters.hook.PlaceholderAPIHook;
import me.hhitt.disasters.listener.BlockBreakListener;
import me.hhitt.disasters.listener.BlockPlaceListener;
import me.hhitt.disasters.listener.DisasterItemListener;
import me.hhitt.disasters.listener.ExplosionListener;
import me.hhitt.disasters.listener.HealthRegenListener;
import me.hhitt.disasters.listener.ItemPickupListener;
import me.hhitt.disasters.listener.JumpPadListener;
import me.hhitt.disasters.listener.PlayerDamageListener;
import me.hhitt.disasters.listener.PlayerDeathListener;
import me.hhitt.disasters.listener.PlayerJoinListener;
import me.hhitt.disasters.listener.PlayerJumpListener;
import me.hhitt.disasters.listener.PlayerLeaveListener;
import me.hhitt.disasters.listener.PlayerMoveListener;
import me.hhitt.disasters.service.DefinitionToggleService;
import me.hhitt.disasters.sidebar.SidebarService;
import me.hhitt.disasters.storage.data.Data;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Filer;
import me.hhitt.disasters.util.Lobby;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Disasters extends JavaPlugin {

    private static Disasters instance;
    private ArenaManager arenaManager;
    private SidebarService sidebarService;
    private boolean bungeeChannelRegistered;
    private boolean enablingComplete;

    public static Disasters getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            initStorage();
            initHooks();
            initSidebar();
            registerCommands();
            registerListeners();
            initDisasters();
            initBungee();
            ItemDropManager.loadConfig();
            enablingComplete = true;
        } catch (Exception e) {
            getLogger().severe("Failed to enable plugin: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onDisable() {
        enablingComplete = false;
        if (sidebarService != null) {
            sidebarService.shutdown();
        }
        if (arenaManager != null) {
            for (Arena arena : arenaManager.getArenas()) {
                arena.getGameSession().finish(FinishReason.PLUGIN_DISABLE);
            }
        }
        ItemDropManager.clearAll();
        if (bungeeChannelRegistered) {
            getServer().getMessenger().unregisterOutgoingPluginChannel(this);
            bungeeChannelRegistered = false;
        }
        Data.shutdown();
        instance = null;
    }

    private void initStorage() {
        Filer.createFolders();
        FileManager.initialize();
        Lobby.setLocation();
        Data.init();
    }

    private void initHooks() {
        final Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if (!(plugin instanceof WorldEditPlugin)) {
            getLogger().severe("WorldEdit plugin not found or wrong type. Disasters cannot start without WorldEdit.");
            throw new IllegalStateException("WorldEdit plugin not found or wrong type");
        }
        final WorldEditPlugin worldEditPlugin = (WorldEditPlugin) plugin;

        arenaManager = new ArenaManager(worldEditPlugin);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(arenaManager).register();
        }
    }

    private void initSidebar() {
        sidebarService = new SidebarService(arenaManager);
    }

    private void registerCommands() {
        final DefinitionToggleService definitionToggleService = new DefinitionToggleService();
        final ArenaCommand arenaCommand = new ArenaCommand(arenaManager);
        final DisastersCommand disastersCommand = new DisastersCommand(arenaManager, sidebarService, definitionToggleService);
        bindCommand("arena", arenaCommand);
        bindCommand("disasters", disastersCommand);
    }

    private void bindCommand(final String name, final TabExecutor executor) {
        final PluginCommand command = getCommand(name);
        if (command == null) {
            throw new IllegalStateException("Command /" + name + " is missing from plugin.yml");
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new ItemPickupListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new HealthRegenListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJumpListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new JumpPadListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new DisasterItemListener(arenaManager), this);
    }

    private void initDisasters() {
        new DisasterTask().runTaskTimer(this, 0L, 20L);
    }

    private void initBungee() {
        final Configuration config = FileManager.get("config");
        if (config != null && config.getBoolean("bungee.enabled", false)) {
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            bungeeChannelRegistered = true;
            getLogger().info("BungeeCord mode enabled! Players will be sent to server: " + config.getString("bungee.server", "lobby"));
        }
    }
}
