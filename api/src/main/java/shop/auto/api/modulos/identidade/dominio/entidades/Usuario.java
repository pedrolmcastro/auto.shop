package shop.auto.api.modulos.identidade.dominio.entidades;

import shop.auto.api.compartilhado.dominio.entidades.Entidade;
import shop.auto.api.compartilhado.dominio.objetosdevalor.UUIDv7;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.NomeUsuario;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida;

public class Usuario extends Entidade {
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
