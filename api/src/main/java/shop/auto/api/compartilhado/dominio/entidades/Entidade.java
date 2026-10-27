package shop.auto.api.compartilhado.dominio.entidades;

import shop.auto.api.compartilhado.dominio.objetosdevalor.UUIDv7;

public abstract class Entidade {
    private final UUIDv7 id;

    protected Entidade(final UUIDv7 id) {
        this.id = id;
    }

    @Override
    public final boolean equals(final Object outra) {
        return outra instanceof Entidade entidade && this.id.equals(entidade.id);
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }

    public final UUIDv7 id() {
        return id;
    }
}
