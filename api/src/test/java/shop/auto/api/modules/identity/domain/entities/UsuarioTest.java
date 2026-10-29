package shop.auto.api.modules.identity.domain.entities;

import com.password4j.SecureString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import shop.auto.api.Fixtures;
import shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario;
import shop.auto.api.modules.identity.domain.valueobjects.SenhaProtegida;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;

public final class UsuarioTest {
    private static final NomeUsuario NOME = new NomeUsuario("admin");

    private static final SenhaProtegida SENHA = SenhaProtegida
        .proteger(new SecureString("admin".toCharArray()), Fixtures.pepper());

    @Nested
    final class Criar {
        @Test
        void deveCriarUsuario() {
            final var usuario = Usuario.criar(NOME, SENHA);

            assertThat(usuario.nome()).isEqualTo(NOME);
            assertThat(usuario.senha()).isEqualTo(SENHA);
        }

        @Test
        void deveGerarId() {
            final var primeiro = Usuario.criar(NOME, SENHA);
            final var segundo = Usuario.criar(NOME, SENHA);

            assertThat(primeiro.id()).isNotEqualTo(segundo.id());
        }
    }

    @Nested
    final class Nome {
        @Test
        void deveExporNomeDoConstrutor() {
            assertThat(new Usuario(UUIDv7.criar(), NOME, SENHA).nome()).isEqualTo(NOME);
        }
    }

    @Nested
    final class Senha {
        @Test
        void deveExporSenhaDoConstrutor() {
            assertThat(new Usuario(UUIDv7.criar(), NOME, SENHA).senha()).isEqualTo(SENHA);
        }
    }
}
