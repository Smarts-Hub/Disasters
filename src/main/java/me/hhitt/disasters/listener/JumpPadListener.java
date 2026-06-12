package me.hhitt.disasters.listener;

import me.hhitt.disasters.arena.Arena;
import me.hhitt.disasters.arena.ArenaManager;
import me.hhitt.disasters.model.arena.JumpPad;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JumpPadListener implements Listener {

    private static final class CooldownKey {
        private final UUID player;
        private final String padId;

        CooldownKey(final UUID player, final String padId) {
            this.player = player;
            this.padId = padId;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof CooldownKey)) {
                return false;
            }
            final CooldownKey that = (CooldownKey) object;
            return player.equals(that.player) && padId.equals(that.padId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player, padId);
        }
    }

    private final ArenaManager arenaManager;
    private final ConcurrentHashMap<CooldownKey, Long> cooldowns = new ConcurrentHashMap<CooldownKey, Long>();

    public JumpPadListener(final ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }
        final Arena arena = arenaManager.getArena(event.getPlayer());
        if (arena == null) {
            return;
        }
        if (arena.getJumpPads().isEmpty()) {
            return;
        }
        final org.bukkit.entity.Player player = event.getPlayer();
        final org.bukkit.Location to = event.getTo();
        final long now = System.currentTimeMillis();
        final JumpPad pad = findPad(arena.getJumpPads(), to.getWorld().getName(), to.getBlockX(), to.getBlockY() - 1, to.getBlockZ());
        if (pad == null) {
            return;
        }
        final CooldownKey cooldownKey = new CooldownKey(player.getUniqueId(), pad.getId());
        final Long next = cooldowns.get(cooldownKey);
        if (next != null && now < next) {
            return;
        }
        cooldowns.put(cooldownKey, now + pad.getCooldownTicks() * 50L);

        final Vector dir = player.getLocation().getDirection();
        final Vector v = new Vector(dir.getX(), 0.0, dir.getZ());
        if (v.lengthSquared() > 0.0) {
            v.normalize().multiply(pad.getPowerForward());
        }
        v.setY(pad.getPowerY());
        player.setVelocity(v);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
    }

    private JumpPad findPad(final List<JumpPad> pads, final String world, final int x, final int y, final int z) {
        for (final JumpPad pad : pads) {
            if (pad.getLocation().getWorld() == null) {
                continue;
            }
            if (!pad.getLocation().getWorld().getName().equals(world)) {
                continue;
            }
            if (pad.getLocation().getBlockX() == x && pad.getLocation().getBlockY() == y && pad.getLocation().getBlockZ() == z) {
                return pad;
            }
        }
        return null;
    }
}
