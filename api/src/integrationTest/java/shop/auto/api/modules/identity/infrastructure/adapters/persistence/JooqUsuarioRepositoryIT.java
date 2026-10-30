package shop.auto.api.modules.identity.infrastructure.adapters.persistence;

import com.password4j.SecureString;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import shop.auto.api.Config;
import shop.auto.api.modules.identity.application.ports.providers.PepperProvider;
import shop.auto.api.modules.identity.application.ports.repositories.UsuarioRepository;
import shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario;
import shop.auto.api.modules.identity.domain.valueobjects.SenhaProtegida;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.Usuario.USUARIO;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@Import(value = Config.class)
@Transactional
class JooqUsuarioRepositoryIT {
    @Nested
    final class Buscar {
        @Test
        void deveRetornarUsuarioQuandoEncontrado(
            @Autowired final DSLContext dsl,
            @Autowired final UsuarioRepository repositorio,
            @Autowired final PepperProvider pepperProvider
        ) {
            final var id = UUIDv7.criar();
            final var nome = new NomeUsuario("nome");
            final var senha = new SecureString("senha".toCharArray());

            try (final var pepper = pepperProvider.obter()) {
                final var senhaProtegida = SenhaProtegida.proteger(senha, pepper);

                dsl
                    .insertInto(USUARIO, USUARIO.ID, USUARIO.NOME, USUARIO.SENHA)
                    .values(id.valor(), nome.valor(), senhaProtegida.valor())
                    .execute();

                final var usuario = repositorio.buscar(nome).orElseThrow();

                assertThat(usuario.id()).isEqualTo(id);
                assertThat(usuario.nome()).isEqualTo(nome);
                assertThat(usuario.senha()).isEqualTo(senhaProtegida);
            }
        }

        @Test
        void deveRetornarVazioQuandoNaoEncontrado(@Autowired final UsuarioRepository repositorio) {
            assertThat(repositorio.buscar(new NomeUsuario("nome"))).isEmpty();
        }
    }
}
