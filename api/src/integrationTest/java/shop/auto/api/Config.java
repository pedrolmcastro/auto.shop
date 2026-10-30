package shop.auto.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.postgresql.PostgreSQLContainer;
import shop.auto.api.modules.identity.application.ports.providers.PepperProvider;
import shop.auto.api.modules.identity.domain.resources.Pepper;

@TestConfiguration(proxyBeanMethods = false)
public class Config {
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
