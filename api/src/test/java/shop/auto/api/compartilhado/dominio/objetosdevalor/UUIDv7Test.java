package shop.auto.api.compartilhado.dominio.objetosdevalor;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.enums.UuidVariant;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import shop.auto.api.compartilhado.dominio.objetosdevalor.UUIDv7.InvalidoException.VarianteInvalidaException;
import shop.auto.api.compartilhado.dominio.objetosdevalor.UUIDv7.InvalidoException.VersaoInvalidaException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class UUIDv7Test {
    @Nested
    final class Construtor {
        @Test
        void deveAceitarUUIDv7() {
            final var valor = UuidCreator.getTimeOrderedEpoch();
            assertThat(new UUIDv7(valor).valor()).isEqualTo(valor);
        }

        @Test
        void deveRejeitarUUIDInvalido() {
            final var valor = new UUID(0xF000L, 0xE000000000000000L);
            assertThatThrownBy(() -> new UUIDv7(valor)).isInstanceOf(UUIDv7.InvalidoException.class);
        }

        @ParameterizedTest
        @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = "VARIANT_STANDARD", value = UuidVariant.class)
        void deveRejeitarVarianteDiferente(final UuidVariant variante) {
            final var valor = new UUID(0x7000L, (long) variante.getValue() << 61);
            assertThatThrownBy(() -> new UUIDv7(valor))
                .isExactlyInstanceOf(VarianteInvalidaException.class)
                .hasMessage("UUIDv7 deve ter variante 2 mas tem %d", variante.getValue());
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15 })
        void deveRejeitarVersaoDiferente(final int versao) {
            final var valor = new UUID((long) versao << 12, Long.MIN_VALUE);
            assertThatThrownBy(() -> new UUIDv7(valor))
                .isExactlyInstanceOf(VersaoInvalidaException.class)
                .hasMessage("UUIDv7 deve ter versão 7 mas tem versão %d", versao);
        }
    }

    @Nested
    final class Criar {
        @Test
        void deveCriarUUIDsDiferentesOrdenados() {
            assertThat(UUIDv7.criar()).isLessThan(UUIDv7.criar());
        }

        @Test
        void deveCriarUUIDv7() {
            final var valor = UUIDv7.criar().valor();
            assertThat(valor.version()).isEqualTo(7);
            assertThat(valor.variant()).isEqualTo(UuidVariant.VARIANT_STANDARD.getValue());
        }
    }

    @Nested
    final class ToString {
        @Test
        void formata() {
            final var string = "0192f37c-8000-7000-8000-000000000001";
            assertThat(new UUIDv7(UUID.fromString(string))).hasToString(string);
        }
    }
}
