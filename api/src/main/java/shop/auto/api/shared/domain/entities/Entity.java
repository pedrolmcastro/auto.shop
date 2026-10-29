package shop.auto.api.shared.domain.entities;

import shop.auto.api.shared.domain.valueobjects.UUIDv7;

public abstract class Entity {
    private final UUIDv7 id;

    protected Entity(final UUIDv7 id) {
        this.id = id;
    }

    @Override
    public final boolean equals(final Object outra) {
        return outra instanceof Entity entidade && this.id.equals(entidade.id);
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }

    public final UUIDv7 id() {
        return id;
    }
}
