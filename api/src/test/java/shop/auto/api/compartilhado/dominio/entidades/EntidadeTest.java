package shop.auto.api.compartilhado.dominio.entidades;

import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import shop.auto.api.compartilhado.dominio.objetosdevalor.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;

public final class EntidadeTest {
    @Nested
    final class Equals {
        @Test
        void deveSerFalsoQuandoIdsSaoDiferentes() {
            final Entidade entidade = new Entidade(UUIDv7.criar()) {};
            final Entidade outra = new Entidade(UUIDv7.criar()) {};
            assertThat(entidade).isNotEqualTo(outra);
        }

        @Test
        void deveSerFalsoQuandoOutroNaoForEntidade() {
            assertThat(new Entidade(UUIDv7.criar()) {}).isNotEqualTo(new Object());
        }

        @Test
        void deveSerVerdadeiroQuandoIdsSaoIguais() {
            final var uuid = UuidCreator.getTimeOrderedEpoch();
            final Entidade entidade = new Entidade(new UUIDv7(uuid)) {};
            final Entidade outra = new Entidade(new UUIDv7(uuid)) {};
            assertThat(entidade).isEqualTo(outra);
        }
    }

    @Nested
    final class HashCode {
        @Test
        void deveSerBaseadoNoId() {
            final var id = UUIDv7.criar();
            assertThat(new Entidade(id) {}).hasSameHashCodeAs(id);
        }
    }

    @Nested
    final class Id {
        @Test
        void deveExporIdDoConstrutor() {
            final var id = UUIDv7.criar();
            assertThat(new Entidade(id) {}.id()).isEqualTo(id);
        }
    }
}
