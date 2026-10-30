package shop.auto.api;

import shop.auto.api.modules.identity.application.ports.repositories.UsuarioRepository;
import shop.auto.api.modules.identity.domain.entities.Usuario;
import shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario;

public final class IntegrationFixtures {
    private final UsuarioRepository usuarios;

    public IntegrationFixtures(final UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario admin() {
        return this.usuarios
            .buscar(new NomeUsuario("admin"))
            .orElseThrow(() -> new IllegalStateException("admin não encontrado"));
    }
}
