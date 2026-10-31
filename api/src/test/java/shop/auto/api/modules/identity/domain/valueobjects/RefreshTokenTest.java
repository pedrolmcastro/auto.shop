package shop.auto.api.modules.identity.domain.valueobjects;

import java.util.Base64;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class RefreshTokenTest {
    private static final String VALOR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn-_0";

    @Nested
    final class Construtor {
        @Test
        void deveAceitarTokenCanonicoUrlSafe() {
            assertThat(new RefreshToken(VALOR).valor()).isEqualTo(VALOR);
        }

        @Test
        void deveRejeitarCodificacaoNaoCanonica() {
            assertThatThrownBy(() -> new RefreshToken("A".repeat(42) + "B"))
                .isExactlyInstanceOf(RefreshToken.InvarianteException.CodificacaoNaoCanonica.class)
                .hasMessage("Refresh token deve usar codificação Base64 URL-safe canônica");
        }

        @ParameterizedTest
        @ValueSource(strings = { "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn-_",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn-_0=", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn-+0" })
        void deveRejeitarFormatoInvalido(final String valor) {
            assertThatThrownBy(() -> new RefreshToken(valor))
                .isExactlyInstanceOf(RefreshToken.InvarianteException.FormatoInvalido.class)
                .hasMessage("Refresh token deve conter 43 caracteres Base64 URL-safe sem padding");
        }
    }

    @Nested
    final class Criar {
        @Test
        void deveGerarSegredosDe256Bits() {
            final var primeiro = RefreshToken.criar();
            final var segundo = RefreshToken.criar();

            assertThat(Base64.getUrlDecoder().decode(primeiro.valor())).hasSize(32);
            assertThat(primeiro.valor()).isNotEqualTo(segundo.valor());
        }
    }

    @Nested
    final class ToString {
        @Test
        void deveOcultarValor() {
            assertThat(new RefreshToken(VALOR)).hasToString("RefreshToken[valor=*]");
        }
    }
}
