package shop.auto.api.shared.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

public final class AsciiTest {
    @Nested
    public final class ELetraMinuscula {
        @ParameterizedTest
        @ValueSource(chars = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' })
        void deveSerFalsoQuandoForLetraAsciiMaiuscula(final char codePoint) {
            assertThat(Ascii.eLetraMinuscula(codePoint)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(ints = { 'Á', 'á', 'Ç', 'ç', 'Ω', 'Ａ', 'ａ' })
        void deveSerFalsoQuandoForLetraNaoAscii(final int codePoint) {
            assertThat(Ascii.eLetraMinuscula(codePoint)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(ints = { '\0', '\n', ' ', '0', '.', '_', 0x007F, -1, Integer.MAX_VALUE })
        void deveSerFalsoQuandoNaoForLetra(final int codePoint) {
            assertThat(Ascii.eLetraMinuscula(codePoint)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z' })
        void deveSerVerdadeiroQuandoForLetraAsciiMinuscula(final char codePoint) {
            assertThat(Ascii.eLetraMinuscula(codePoint)).isTrue();
        }
    }
}
