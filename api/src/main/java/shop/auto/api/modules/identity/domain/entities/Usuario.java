package shop.auto.api.modules.identity.domain.entities;

import shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario;
import shop.auto.api.modules.identity.domain.valueobjects.SenhaProtegida;
import shop.auto.api.shared.domain.entities.Entity;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;

public class Usuario extends Entity {
    private final NomeUsuario nome;
    private final SenhaProtegida senha;

    public Usuario(final UUIDv7 id, final NomeUsuario nome, final SenhaProtegida senha) {
        super(id);
        this.nome = nome;
        this.senha = senha;
    }

    public static Usuario criar(final NomeUsuario nome, final SenhaProtegida senha) {
        return new Usuario(UUIDv7.criar(), nome, senha);
    }

    public NomeUsuario nome() {
        return nome;
    }

    public SenhaProtegida senha() {
        return senha;
    }
}
