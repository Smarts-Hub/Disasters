package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.service.DeathMessageService;
import me.hhitt.disasters.util.Msg;
import me.hhitt.disasters.util.Notify;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HotPotato implements Disaster, Listener, TriggerTrackedDisaster {

    private static final NamespacedKey HOT_POTATO_KEY = new NamespacedKey(Disasters.getInstance(), "hot_potato");

    private int triggerCount = 0;

    private final List<Arena> arenas = new ArrayList<>();
    private final ConcurrentHashMap<Arena, UUID> holders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Arena, Integer> countdowns = new ConcurrentHashMap<>();
    private final int interval = 10;

    private static final Random RANDOM = new Random();

    public static ItemStack makePotato() {
        ItemStack stack = new ItemStack(Material.POTATO, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text("Hot Potato").color(NamedTextColor.RED));
        meta.getPersistentDataContainer().set(HOT_POTATO_KEY, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
    }

    public static boolean isPotato(final ItemStack item) {
        if (item == null || item.getType() != Material.POTATO) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(HOT_POTATO_KEY, PersistentDataType.BYTE);
    }

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }

    @Override
    public void start(final Arena arena) {
        arenas.add(arena);
        pickNewHolder(arena);
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        Notify.disaster(arena, "hot-potato");
    }

    @Override
    public void pulse(final int time) {
        for (Arena arena : new ArrayList<>(arenas)) {
            UUID holderId = holders.get(arena);
            if (holderId == null) {
                pickNewHolder(arena);
                continue;
            }
            Player holder = Bukkit.getPlayer(holderId);
            if (holder == null) {
                pickNewHolder(arena);
                continue;
            }
            if (!hasPotato(holder)) {
                pickNewHolder(arena);
                continue;
            }
            Integer currentCountdown = countdowns.get(arena);
            int remain = (currentCountdown != null ? currentCountdown : interval) - 1;
            countdowns.put(arena, remain);
            Msg.sendActionbar(holder, "<red>Hot Potato: " + remain + "s");
            if (remain <= 0) {
                triggerCount++;
                DeathMessageService.mark(holder, "hot-potato");
                holder.getWorld().createExplosion(holder.getLocation(), 4f, false, true);
                if (holder.isOnline()) {
                    holder.setHealth(0.0);
                }
                removePotatoFromAll(arena);
                holders.remove(arena);
                countdowns.remove(arena);
                pickNewHolder(arena);
            }
        }
    }

    @Override
    public void stop(final Arena arena) {
        removePotatoFromAll(arena);
        holders.remove(arena);
        countdowns.remove(arena);
        arenas.remove(arena);
        HandlerList.unregisterAll(this);
    }

    private void pickNewHolder(final Arena arena) {
        if (arena.getAlive().isEmpty()) {
            return;
        }
        Player newHolder = arena.getAlive().get(RANDOM.nextInt(arena.getAlive().size()));
        holders.put(arena, newHolder.getUniqueId());
        countdowns.put(arena, interval);
        newHolder.getInventory().addItem(makePotato());
    }

    private void removePotatoFromAll(final Arena arena) {
        for (Player player : arena.getPlaying()) {
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (isPotato(contents[i])) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }

    private boolean hasPotato(final Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isPotato(item)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        Arena arena = null;
        for (Arena a : arenas) {
            if (a.isPlayerValid(event.getPlayer())) {
                arena = a;
                break;
            }
        }
        if (arena == null) {
            return;
        }
        if (!event.getPlayer().getUniqueId().equals(holders.get(arena))) {
            return;
        }
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : arena.getAlive()) {
            if (p.getUniqueId().equals(event.getPlayer().getUniqueId())) {
                continue;
            }
            double d = p.getLocation().distanceSquared(event.getPlayer().getLocation());
            if (d < minDist) {
                minDist = d;
                nearest = p;
            }
        }
        if (nearest == null) {
            return;
        }
        if (event.getPlayer().getLocation().distance(nearest.getLocation()) <= 1.5) {
            ItemStack hot = null;
            for (ItemStack item : event.getPlayer().getInventory().getContents()) {
                if (isPotato(item)) {
                    hot = item;
                    break;
                }
            }
            if (hot == null) {
                return;
            }
            event.getPlayer().getInventory().removeItem(hot);
            nearest.getInventory().addItem(hot.clone());
            holders.put(arena, nearest.getUniqueId());
            countdowns.put(arena, interval);
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (isPotato(event.getItem())) {
            event.setCancelled(true);
        }
    }
}
