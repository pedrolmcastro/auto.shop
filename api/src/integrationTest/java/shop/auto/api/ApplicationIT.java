package shop.auto.api;

import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.currentCatalog;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
class ApplicationIT {
    @Test
    void deveInicializarComPostgreSQL(@Autowired final DSLContext dsl, @Autowired final Flyway flyway) {
        assertThat(dsl.fetchValue(currentCatalog())).isEqualTo("api_auto_shop_test");

        assertThat(dsl.meta().getSchemas("identity")).isNotEmpty();

        assertThat(flyway.info().applied()).isNotEmpty();
        assertThat(flyway.info().pending()).isEmpty();

        flyway.validate();

        assertThat(flyway.migrate().migrationsExecuted).isZero();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Containers {
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            final var postgres = new PostgreSQLContainer("postgres:18.6");
            postgres.withDatabaseName("api_auto_shop_test");
            return postgres;
        }
    }
}
