package shop.auto.api;

import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;
import shop.auto.api.modules.identity.application.ports.providers.PepperProvider;
import shop.auto.api.modules.identity.application.ports.repositories.UsuarioRepository;
import shop.auto.api.modules.identity.domain.resources.Pepper;

@TestConfiguration(proxyBeanMethods = false)
public class IntegrationConfig {
    @Bean
    public DynamicPropertyRegistrar testDynamicPropertyRegistrar() throws IOException {
        final var privada = new ClassPathResource("jwt-test-private.pem").getFile();
        final var publica = new ClassPathResource("jwt-test-public.pem").getFile();

        return registro -> {
            registro.add("identity.jwt.private-key-file", privada::getAbsolutePath);
            registro.add("identity.jwt.public-key-file", publica::getAbsolutePath);
        };
    }

    @Bean
    public IntegrationFixtures testFixtures(final UsuarioRepository usuarios) {
        return new IntegrationFixtures(usuarios);
    }

    @Bean
    @Primary
    public PepperProvider testPepperProvider() {
        return () -> new Pepper(new byte[Pepper.COMPRIMENTO]);
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer testPostgreSQLContainer() {
        final var postgres = new PostgreSQLContainer("postgres:18.6");
        postgres.withDatabaseName("api_auto_shop_test");
        return postgres;
    }
}
