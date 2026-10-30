package shop.auto.api.modules.identity.infrastructure.migration;

import com.password4j.SecureString;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import shop.auto.api.Config;
import shop.auto.api.modules.identity.application.ports.providers.PepperProvider;
import shop.auto.api.modules.identity.domain.valueobjects.SenhaProtegida;
import static org.assertj.core.api.Assertions.assertThat;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.Usuario.USUARIO;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@Import(value = Config.class)
class V3__insert_usuario_adminIT {
    @Test
    void deveInserirAdmin(@Autowired final DSLContext dsl, @Autowired final PepperProvider pepperProvider) {
        final var admin = dsl.selectFrom(USUARIO).where(USUARIO.NOME.eq("admin")).fetchSingle();
        final var senha = SenhaProtegida.reconstruir(admin.getSenha());

        try (final var pepper = pepperProvider.obter()) {
            assertThat(admin.getNome()).isEqualTo("admin");
            assertThat(senha.verificar(new SecureString("admin".toCharArray()), pepper)).isTrue();
        }
    }

    @Test
    void naoDeveRecriarAdmin(@Autowired final DSLContext dsl, @Autowired final Flyway flyway) {
        final var admin = dsl.selectFrom(USUARIO).where(USUARIO.NOME.eq("admin")).fetchSingle();
        flyway.migrate();
        assertThat(dsl.selectFrom(USUARIO).where(USUARIO.NOME.eq("admin")).fetchSingle()).isEqualTo(admin);
    }
}
