package me.hhitt.disasters.util;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Lobby {

    private static Location location = new Location(null, 0.0, 0.0, 0.0);
    private static final Map<UUID, InventorySnapshot> inventorySnapshots = new ConcurrentHashMap<>();

    private Lobby() {
    }

    public static void setLocation() {
        Configuration config = FileManager.get("config");
        if (config == null) {
            throw new IllegalStateException("Config not loaded");
        }
        Location loc = new Location(
            Objects.requireNonNull(Bukkit.getWorld(config.getString("lobby.world")), "lobby.world is null"),
            config.getDouble("lobby.x"),
            config.getDouble("lobby.y"),
            config.getDouble("lobby.z"),
            (float) config.getDouble("lobby.yaw"),
            (float) config.getDouble("lobby.pitch")
        );
        Lobby.location = loc;
    }

    public static void savePlayerState(Player player) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        ItemStack[] clonedContents = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            clonedContents[i] = contents[i] != null ? contents[i].clone() : null;
        }
        ItemStack[] armor = inv.getArmorContents();
        ItemStack[] clonedArmor = new ItemStack[armor.length];
        for (int i = 0; i < armor.length; i++) {
            clonedArmor[i] = armor[i] != null ? armor[i].clone() : null;
        }
        ItemStack offhand = inv.getItemInOffHand().clone();
        InventorySnapshot snapshot = new InventorySnapshot(clonedContents, clonedArmor, offhand);
        inventorySnapshots.put(player.getUniqueId(), snapshot);
    }

    public static boolean restorePlayerState(Player player) {
        InventorySnapshot snapshot = inventorySnapshots.remove(player.getUniqueId());
        if (snapshot == null) {
            return false;
        }
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setContents(snapshot.contents);
        inv.setArmorContents(snapshot.armor);
        inv.setItemInOffHand(snapshot.offhand);
        return true;
    }

    public static void teleportPlayer(Player player) {
        if (isBungeeEnabled()) {
            resetPlayerState(player);
            sendToServer(player);
            return;
        }
        player.teleport(location);
        resetPlayerState(player);
    }

    public static void teleportAtEnd(Arena arena) {
        Bukkit.getScheduler().runTaskLater(Disasters.getInstance(), (Runnable) () -> {
            arena.getPlaying().forEach(Lobby::teleportPlayer);
            arena.clear();
        }, 60L);
    }

    private static void resetPlayerState(Player player) {
        player.getActivePotionEffects().clear();
        if (!restorePlayerState(player)) {
            player.getInventory().clear();
        }
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setLevel(0);
        player.setExp(0.0f);
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().clear();
    }

    private static boolean isBungeeEnabled() {
        Configuration config = FileManager.get("config");
        return config != null && config.getBoolean("bungee.enabled", false);
    }

    private static void sendToServer(Player player) {
        Configuration config = FileManager.get("config");
        if (config == null) {
            return;
        }
        String serverName = config.getString("bungee.server", "lobby");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(out);
        try {
            data.writeUTF("Connect");
            data.writeUTF(serverName);
            player.sendPluginMessage(Disasters.getInstance(), "BungeeCord", out.toByteArray());
        } catch (IOException e) {
            Disasters.getInstance().getLogger().warning("Failed to send BungeeCord message: " + e.getMessage());
        }
    }

    private static final class InventorySnapshot {
        final ItemStack[] contents;
        final ItemStack[] armor;
        final ItemStack offhand;

        InventorySnapshot(ItemStack[] contents, ItemStack[] armor, ItemStack offhand) {
            this.contents = contents;
            this.armor = armor;
            this.offhand = offhand;
        }
    }
}
