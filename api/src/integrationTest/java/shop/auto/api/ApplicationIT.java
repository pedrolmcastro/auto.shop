package shop.auto.api;

import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.currentCatalog;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@Import(value = Config.class)
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
}
