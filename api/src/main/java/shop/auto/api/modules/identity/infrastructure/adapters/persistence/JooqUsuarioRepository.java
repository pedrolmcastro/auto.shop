package shop.auto.api.modules.identity.infrastructure.adapters.persistence;

import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import shop.auto.api.modules.identity.application.ports.repositories.UsuarioRepository;
import shop.auto.api.modules.identity.domain.entities.Usuario;
import shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario;
import shop.auto.api.modules.identity.domain.valueobjects.SenhaProtegida;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.Usuario.USUARIO;

@Repository
public class JooqUsuarioRepository implements UsuarioRepository {
    private final DSLContext dsl;

    public JooqUsuarioRepository(final DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<Usuario> buscar(final NomeUsuario nome) {
        return this.dsl
            .select(USUARIO.ID, USUARIO.NOME, USUARIO.SENHA)
            .from(USUARIO)
            .where(USUARIO.NOME.eq(nome.valor()))
            .fetchOptional(
                registro -> new Usuario(
                    new UUIDv7(registro.value1()),
                    new NomeUsuario(registro.value2()),
                    SenhaProtegida.reconstruir(registro.value3())
                )
            );
    }
}
