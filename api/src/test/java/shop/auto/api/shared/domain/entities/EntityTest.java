package shop.auto.api.shared.domain.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;

public final class EntityTest {
    @Nested
    final class Equals {
        @Test
        void deveSerFalsoQuandoIdsSaoDiferentes() {
            final Entity entidade = new Entity(UUIDv7.criar()) {};
            final Entity outra = new Entity(UUIDv7.criar()) {};
            assertThat(entidade).isNotEqualTo(outra);
        }

        @Test
        void deveSerFalsoQuandoNaoForEntidade() {
            assertThat(new Entity(UUIDv7.criar()) {}).isNotEqualTo(new Object());
        }

        @Test
        void deveSerVerdadeiroQuandoIdsSaoIguais() {
            final var uuid = UuidCreator.getTimeOrderedEpoch();
            final Entity entidade = new Entity(new UUIDv7(uuid)) {};
            final Entity outra = new Entity(new UUIDv7(uuid)) {};
            assertThat(entidade).isEqualTo(outra);
        }
    }

    @Nested
    final class HashCode {
        @Test
        void deveSerBaseadoNoId() {
            final var id = UUIDv7.criar();
            assertThat(new Entity(id) {}).hasSameHashCodeAs(id);
        }
    }

    @Nested
    final class Id {
        @Test
        void deveExporIdDoConstrutor() {
            final var id = UUIDv7.criar();
            assertThat(new Entity(id) {}.id()).isEqualTo(id);
        }
    }
}
