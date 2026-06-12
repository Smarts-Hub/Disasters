package me.hhitt.disasters.disaster;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class DisasterDefinition {
    private final String id;
    private final String displayName;
    private final Class<? extends Disaster> type;
    private final Supplier<? extends Disaster> factory;
    private final Set<String> incompatibleWith;

    public DisasterDefinition(final String id, final String displayName, final Class<? extends Disaster> type, final Supplier<? extends Disaster> factory) {
        this(id, displayName, type, factory, Collections.<String>emptySet());
    }

    public DisasterDefinition(final String id, final String displayName, final Class<? extends Disaster> type, final Supplier<? extends Disaster> factory, final Set<String> incompatibleWith) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.type = Objects.requireNonNull(type, "type");
        this.factory = Objects.requireNonNull(factory, "factory");
        this.incompatibleWith = Collections.unmodifiableSet(new LinkedHashSet<String>(Objects.requireNonNull(incompatibleWith, "incompatibleWith")));
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Class<? extends Disaster> getType() {
        return type;
    }

    public Supplier<? extends Disaster> getFactory() {
        return factory;
    }

    public Set<String> getIncompatibleWith() {
        return incompatibleWith;
    }

    public Disaster factory() {
        final Disaster disaster = factory.get();
        if (disaster == null) {
            throw new IllegalStateException("Disaster factory returned null for " + id);
        }
        return disaster;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DisasterDefinition)) {
            return false;
        }
        final DisasterDefinition that = (DisasterDefinition) object;
        return id.equals(that.id)
            && displayName.equals(that.displayName)
            && type.equals(that.type)
            && factory.equals(that.factory)
            && incompatibleWith.equals(that.incompatibleWith);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, type, factory, incompatibleWith);
    }

    @Override
    public String toString() {
        return "DisasterDefinition(id=" + id
            + ", displayName=" + displayName
            + ", type=" + type
            + ", factory=" + factory
            + ", incompatibleWith=" + incompatibleWith
            + ')';
    }
}
