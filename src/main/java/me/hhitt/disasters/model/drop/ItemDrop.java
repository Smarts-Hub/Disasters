package me.hhitt.disasters.model.drop;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;

import java.util.Objects;

/**
 * Tracks a disaster item drop: the dropped item entity and its floating name ArmorStand.
 */
public final class ItemDrop {
    private final Item item;
    private final ArmorStand label;
    private final int spawnTime;

    public ItemDrop(final Item item, final ArmorStand label, final int spawnTime) {
        this.item = Objects.requireNonNull(item, "item");
        this.label = Objects.requireNonNull(label, "label");
        this.spawnTime = spawnTime;
    }

    public Item getItem() {
        return item;
    }

    public ArmorStand getLabel() {
        return label;
    }

    public int getSpawnTime() {
        return spawnTime;
    }

    public void remove() {
        if (!item.isDead()) {
            item.remove();
        }
        if (!label.isDead()) {
            label.remove();
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ItemDrop)) {
            return false;
        }
        final ItemDrop itemDrop = (ItemDrop) object;
        return spawnTime == itemDrop.spawnTime
            && item.equals(itemDrop.item)
            && label.equals(itemDrop.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, label, spawnTime);
    }

    @Override
    public String toString() {
        return "ItemDrop(item=" + item
            + ", label=" + label
            + ", spawnTime=" + spawnTime
            + ')';
    }
}
