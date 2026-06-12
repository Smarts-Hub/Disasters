package me.hhitt.disasters.disaster.impl;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.disaster.Disaster;
import me.hhitt.disasters.disaster.TriggerTrackedDisaster;
import me.hhitt.disasters.util.Notify;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AnvilRain implements Disaster, TriggerTrackedDisaster {

    private int triggerCount = 0;

    private final List<Arena> arenas = new ArrayList<>();
    private final Map<Arena, List<FallingBlock>> anvils = new HashMap<>();

    @Override
    public int getTriggerCount() {
        return triggerCount;
    }

    @Override
    public void start(Arena arena) {
        arenas.add(arena);
        anvils.put(arena, new ArrayList<FallingBlock>());
        Notify.disaster(arena, "anvil-rain");
    }

    @Override
    public void pulse(int time) {
        if (time % 2 != 0) return;
        for (Arena arena : new ArrayList<>(arenas)) {
            List<FallingBlock> list = anvils.get(arena);
            if (list == null) continue;
            list.removeIf(FallingBlock::isDead);

            for (Player player : arena.getAlive()) {
                Location target = player.getLocation().clone().add(
                    ThreadLocalRandom.current().nextDouble(-6.0, 6.0), 0.0, ThreadLocalRandom.current().nextDouble(-6.0, 6.0)
                );
                if (!arena.getBorderService().isLocationInArena(target)) continue;
                Location spawn = target.clone().add(0.0, ThreadLocalRandom.current().nextDouble(20.0, 28.0), 0.0);
                FallingBlock anvil = spawn.getWorld().spawn(spawn, FallingBlock.class);
                anvil.setBlockData(Material.ANVIL.createBlockData());
                anvil.setDropItem(false);
                anvil.setHurtEntities(true);
                anvil.setDamagePerBlock(8f);
                anvil.setMaxDamage(40);
                list.add(anvil);
                triggerCount++;
            }
        }
    }

    @Override
    public void stop(Arena arena) {
        List<FallingBlock> removed = anvils.remove(arena);
        if (removed != null) {
            for (FallingBlock anvil : removed) {
                if (!anvil.isDead()) anvil.remove();
            }
        }
        arenas.remove(arena);
    }
}
