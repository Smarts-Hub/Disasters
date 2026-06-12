package me.hhitt.disasters.game.modification;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class GameModificationDefinition {
    private final String id;
    private final String displayName;
    private final Material material;
    private final List<String> description;
    private final Supplier<? extends GameModification> factory;

    public GameModificationDefinition(final String id, final String displayName, final Material material, final List<String> description, final Supplier<? extends GameModification> factory) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.material = Objects.requireNonNull(material, "material");
        this.description = Collections.unmodifiableList(new ArrayList<String>(Objects.requireNonNull(description, "description")));
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public List<String> getDescription() {
        return description;
    }

    public Supplier<? extends GameModification> getFactory() {
        return factory;
    }

    public GameModification factory() {
        final GameModification modification = factory.get();
        if (modification == null) {
            throw new IllegalStateException("Game modification factory returned null for " + id);
        }
        return modification;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof GameModificationDefinition)) {
            return false;
        }
        final GameModificationDefinition that = (GameModificationDefinition) object;
        return id.equals(that.id)
            && displayName.equals(that.displayName)
            && material == that.material
            && description.equals(that.description)
            && factory.equals(that.factory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, material, description, factory);
    }

    @Override
    public String toString() {
        return "GameModificationDefinition(id=" + id
            + ", displayName=" + displayName
            + ", material=" + material
            + ", description=" + description
            + ", factory=" + factory
            + ')';
    }
}
