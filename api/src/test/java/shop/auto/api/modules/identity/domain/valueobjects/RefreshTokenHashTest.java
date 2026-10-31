package shop.auto.api.modules.identity.domain.valueobjects;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

public final class RefreshTokenHashTest {
    private static final RefreshToken TOKEN = new RefreshToken("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnop0");
    private static final String SHA256_TOKEN = "10bf9273f866b7756bc7802746147fcad829c19fbb9fffa00bcd6c2db76f5dbc";

    @Nested
    final class Construtor {
        @ParameterizedTest
        @MethodSource(value = "formatosInvalidos")
        void deveRejeitarFormatoInvalido(final String valor) {
            assertThatThrownBy(() -> new RefreshTokenHash(valor))
                .isExactlyInstanceOf(RefreshTokenHash.InvarianteException.FormatoInvalido.class)
                .hasMessage("Hash do refresh token deve conter 64 caracteres hexadecimais minúsculos");
        }

        private static Stream<String> formatosInvalidos() {
            return Stream.of("a".repeat(63), "a".repeat(65), "A".repeat(64), "g".repeat(64));
        }
    }

    @Nested
    final class Hash {
        @Test
        void deveCalcularSha256() {
            assertThat(RefreshTokenHash.hash(TOKEN).valor()).isEqualTo(SHA256_TOKEN);
        }

        @Test
        void devePreservarCausaQuandoAlgoritmoEstiverIndisponivel() {
            final var causa = new NoSuchAlgorithmException("SHA-256 indisponível");

            try (final var digest = mockStatic(MessageDigest.class)) {
                digest.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(causa);

                assertThatThrownBy(() -> assertThat(RefreshTokenHash.hash(TOKEN)).isNotNull())
                    .isExactlyInstanceOf(RefreshTokenHash.InvarianteException.AlgoritmoIndisponivel.class)
                    .hasCause(causa);
            }
        }
    }

    @Nested
    final class ToString {
        @Test
        void deveOcultarValor() {
            assertThat(new RefreshTokenHash(SHA256_TOKEN)).hasToString("RefreshTokenHash[valor=*]");
        }
    }
}
