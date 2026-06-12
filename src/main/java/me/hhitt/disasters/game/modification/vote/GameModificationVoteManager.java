package me.hhitt.disasters.game.modification.vote;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.game.modification.GameModificationDefinition;
import me.hhitt.disasters.game.modification.GameModificationRegistry;
import me.hhitt.disasters.storage.file.Configuration;
import me.hhitt.disasters.storage.file.FileManager;
import me.hhitt.disasters.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import me.hhitt.disasters.Disasters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameModificationVoteManager implements Listener {

    private final Arena arena;
    private final Map<UUID, String> votes = new ConcurrentHashMap<UUID, String>();
    private final Map<UUID, Inventory> openInventories = new ConcurrentHashMap<UUID, Inventory>();
    private boolean resolved = false;
    private List<GameModificationDefinition> enabled = Collections.emptyList();

    public GameModificationVoteManager(final Arena arena) {
        this.arena = arena;
    }

    public void startVote() {
        enabled = GameModificationRegistry.enabledDefinitions(arena);
        if (enabled.isEmpty()) {
            resolved = true;
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, Disasters.getInstance());
        for (final Player player : new ArrayList<Player>(arena.getPlaying())) {
            openGui(player);
        }
    }

    private void openGui(final Player player) {
        final int size;
        if (enabled.size() <= 9) {
            size = 9;
        } else if (enabled.size() <= 18) {
            size = 18;
        } else {
            size = 27;
        }

        final Configuration cfg = FileManager.get("config");
        final String titleRaw = cfg != null ? cfg.getString(
            "game-modifications.voting.inventory-title",
            "<gold><bold>Vote Game Modification"
        ) : "<gold><bold>Vote Game Modification";
        Component title;
        try {
            title = Msg.parse(titleRaw, player);
        } catch (final Exception e) {
            title = Component.text("Vote Game Modification");
        }

        final Inventory inventory = Bukkit.createInventory(null, size, title);
        final int slotOffset = (size - enabled.size()) / 2;

        for (int idx = 0; idx < enabled.size(); idx++) {
            final GameModificationDefinition def = enabled.get(idx);
            final ItemStack item = new ItemStack(def.getMaterial());
            item.editMeta(meta -> {
                meta.displayName(
                    Component.text(def.getDisplayName())
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)
                );
                final List<Component> lore = new ArrayList<Component>();
                for (final String line : def.getDescription()) {
                    lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(lore);
            });
            inventory.setItem(slotOffset + idx, item);
        }

        openInventories.put(player.getUniqueId(), inventory);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = openInventories.get(player.getUniqueId());
        if (inventory == null) {
            return;
        }
        if (event.getInventory() != inventory) {
            return;
        }
        event.setCancelled(true);
        final int slot = event.getRawSlot();
        final int size = inventory.getSize();
        final int slotOffset = (size - enabled.size()) / 2;
        final int idx = slot - slotOffset;
        if (idx < 0 || idx >= enabled.size()) {
            return;
        }
        final GameModificationDefinition def = enabled.get(idx);
        votes.put(player.getUniqueId(), def.getId());
        player.closeInventory();
        player.sendMessage(
            Component.text("You voted for ").color(NamedTextColor.GRAY)
                .append(Component.text(def.getDisplayName()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
        );
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getPlayer();
        openInventories.remove(player.getUniqueId());
    }

    public void cancelVote() {
        for (final UUID uuid : openInventories.keySet()) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.closeInventory();
            }
        }
        openInventories.clear();
        HandlerList.unregisterAll(this);
        resolved = true;
    }

    public List<String> resolveVote() {
        if (resolved) {
            return Collections.emptyList();
        }
        resolved = true;

        for (final UUID uuid : openInventories.keySet()) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.closeInventory();
            }
        }
        openInventories.clear();
        HandlerList.unregisterAll(this);

        if (enabled.isEmpty()) {
            return Collections.emptyList();
        }

        final Configuration cfg = FileManager.get("config");
        final boolean allowDefault = cfg != null && cfg.getBoolean("game-modifications.voting.allow-no-vote-default", true);

        final String winner;
        if (!votes.isEmpty()) {
            final Map<String, Long> counts = votes.values().stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
            final Map.Entry<String, Long> maxEntry = counts.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElse(null);
            if (maxEntry != null) {
                winner = maxEntry.getKey();
            } else {
                winner = enabled.get(0).getId();
            }
        } else if (allowDefault) {
            final String defaultId = cfg != null ? cfg.getString(
                "game-modifications.voting.default", enabled.get(0).getId()
            ) : enabled.get(0).getId();
            boolean found = false;
            for (final GameModificationDefinition def : enabled) {
                if (def.getId().equals(defaultId)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                winner = defaultId;
            } else {
                winner = enabled.get(0).getId();
            }
        } else {
            winner = enabled.get(0).getId();
        }

        votes.clear();
        final List<String> result = new ArrayList<String>();
        result.add(winner);
        return result;
    }
}
