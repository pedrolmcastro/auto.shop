package shop.auto.api.modules.identity.application.ports.repositories;

import java.util.Optional;
import shop.auto.api.modules.identity.domain.entities.Usuario;
import shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario;

public interface UsuarioRepository {
    public abstract Optional<Usuario> buscar(NomeUsuario nome);
}
