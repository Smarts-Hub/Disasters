package me.hhitt.disasters.listener;

import me.hhitt.disasters.Disasters;
import me.hhitt.disasters.arena.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class DisasterItemListener implements Listener {

    private final ArenaManager arenaManager;

    public DisasterItemListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onTntUse(final PlayerInteractEvent event) {
        if (arenaManager.getArena(event.getPlayer()) == null) {
            return;
        }
        final ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getType() == Material.TNT) {
            event.setCancelled(true);
            final org.bukkit.entity.Player player = event.getPlayer();
            if (player.getInventory().containsAtLeast(new ItemStack(Material.TNT), 1)) {
                player.getInventory().removeItem(new ItemStack(Material.TNT, 1));
            }
            final Vector dir = player.getLocation().getDirection();
            final org.bukkit.Location spawn = player.getEyeLocation().add(dir.clone().multiply(1.5));
            final TNTPrimed tnt = player.getWorld().spawn(spawn, TNTPrimed.class);
            tnt.setFuseTicks(40);
            tnt.setYield(4f);
            tnt.setVelocity(dir.clone().multiply(1.2));
        }
    }

    @EventHandler
    public void onWaterBucket(final PlayerBucketEmptyEvent event) {
        if (arenaManager.getArena(event.getPlayer()) == null) {
            return;
        }
        if (event.getBucket() == Material.WATER_BUCKET) {
            Bukkit.getScheduler().runTask(Disasters.getInstance(), new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().getInventory().setItem(event.getHand(), new ItemStack(Material.BUCKET));
                }
            });
        }
    }

    @EventHandler
    public void onMilkDrink(final PlayerItemConsumeEvent event) {
        if (arenaManager.getArena(event.getPlayer()) == null) {
            return;
        }
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            Bukkit.getScheduler().runTask(Disasters.getInstance(), new Runnable() {
                @Override
                public void run() {
                    final org.bukkit.inventory.EquipmentSlot hand = event.getHand();
                    if (hand != null) {
                        event.getPlayer().getInventory().setItem(hand, new ItemStack(Material.BUCKET));
                    }
                }
            });
        }
    }
}
