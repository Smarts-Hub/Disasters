package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.model.entity.DisasterSheep;
import me.hhitt.disasters.util.Notify;
import me.hhitt.disasters.util.SpawnLocationFinder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExplosiveSheep implements Disaster {

    private final Map<Arena, CopyOnWriteArrayList<DisasterSheep>> arenaSheep = new HashMap<>();

    @Override
    public void start(Arena arena) {
        arenaSheep.put(arena, new CopyOnWriteArrayList<DisasterSheep>());
        Notify.disaster(arena, "explosive-sheep");
    }

    @Override
    public void pulse(int time) {
        tick();

        if (time % 5 != 0) return;
        for (Arena arena : new ArrayList<>(arenaSheep.keySet())) {
            for (Player player : arena.getAlive()) {
                spawnSheep(arena, player, 10, 1);
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        CopyOnWriteArrayList<DisasterSheep> removed = arenaSheep.remove(arena);
        if (removed != null) {
            for (DisasterSheep sheep : removed) {
                sheep.remove();
            }
        }
    }

    private void tick() {
        for (CopyOnWriteArrayList<DisasterSheep> sheeps : arenaSheep.values()) {
            for (DisasterSheep sheep : new ArrayList<DisasterSheep>(sheeps)) {
                if (sheep.isAlive()) {
                    sheep.call();
                } else {
                    sheeps.remove(sheep);
                }
            }
        }
    }

    private void spawnSheep(Arena arena, Player player, int radius, int amount) {
        for (int i = 0; i < amount; i++) {
            Location spawnLocation = SpawnLocationFinder.findNearPlayer(arena, player.getLocation(), 3, radius, 5);
            if (spawnLocation.getWorld() == null) continue;
            Sheep bukkitSheep = spawnLocation.getWorld().spawn(spawnLocation, Sheep.class);
            DisasterSheep sheep = new DisasterSheep(arena, bukkitSheep);
            addTntDisplay(sheep);
            CopyOnWriteArrayList<DisasterSheep> list = arenaSheep.get(arena);
            if (list != null) {
                list.add(sheep);
            }
        }
    }

    private void addTntDisplay(DisasterSheep sheep) {
        Sheep entity = sheep.getSheep();
        ArmorStand stand = entity.getWorld().spawn(entity.getLocation(), ArmorStand.class);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.getEquipment().setHelmet(new ItemStack(Material.TNT));
        entity.addPassenger(stand);
    }
}
