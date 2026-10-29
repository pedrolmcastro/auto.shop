package shop.auto.api.modulos.identidade.dominio.recursos;

import com.password4j.SecureString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shop.auto.api.modulos.identidade.dominio.recursos.Pepper.ComprimentoInvalidoException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static shop.auto.api.modulos.identidade.dominio.recursos.Pepper.COMPRIMENTO;

public final class PepperTest {
    @Nested
    final class Close {
        @Test
        void deveLimparValor() throws ReflectiveOperationException {
            final var pepper = mock();

            final var campo = Pepper.class.getDeclaredField("valor");
            campo.setAccessible(true);
            final var valor = (byte[]) campo.get(pepper);

            pepper.close();

            assertThat(campo.get(pepper)).isNull();
            assertThat(valor).containsOnly((byte) 0);
        }

        @Test
        void deveSerIdempotente() {
            final var pepper = mock();

            assertThatCode(() -> {
                pepper.close();
                pepper.close();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    final class Construtor {
        @ParameterizedTest
        @ValueSource(bytes = { -128, -1, 0, 1, 127 })
        void deveAceitarPepperValido(final byte representacao) {
            final var valor = new byte[COMPRIMENTO];
            Arrays.fill(valor, representacao);

            try (final var pepper = new Pepper(valor)) {
                pepper.usar(segredo -> assertThat(HexFormat.of().parseHex(segredo)).containsExactly(valor));
            }
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, COMPRIMENTO - 1, COMPRIMENTO + 1 })
        void deveRejeitarComprimentoInvalido(final int comprimento) {
            final var valor = new byte[comprimento];

            assertThatThrownBy(() -> new Pepper(valor))
                .isExactlyInstanceOf(ComprimentoInvalidoException.class)
                .hasMessage("Pepper deve ter exatamente 32 bytes mas tem %d", comprimento);
        }

        @Test
        void naoClonarValor() {
            final var valor = new byte[COMPRIMENTO];
            Arrays.fill(valor, (byte) 1);

            try (final var pepper = new Pepper(valor)) {
                Arrays.fill(valor, (byte) 0);
                pepper.usar(segredo -> assertThat(segredo.chars()).anyMatch(representacao -> representacao != 0));
            }
        }
    }

    @Nested
    final class ToString {
        @Test
        void deveOcultarValor() {
            try (final var pepper = mock()) {
                assertThat(pepper).hasToString("Pepper[valor=*]");
            }
        }
    }

    @Nested
    final class Usar {
        @Test
        void deveLimparParametroQuandoFalha() {
            final var excecao = new RuntimeException();

            final var referencia = new ArrayList<SecureString>();

            try (final var pepper = mock()) {
                assertThatThrownBy(() -> pepper.usar(segredo -> {
                    referencia.add(segredo);
                    throw excecao;
                })).isSameAs(excecao);

                assertThat(referencia.getFirst().chars()).containsOnly(0);
            }
        }

        @Test
        void deveLimparParametroQuandoTermina() {
            final var referencia = new ArrayList<SecureString>();

            try (final var pepper = mock()) {
                pepper.usar(referencia::add);
                assertThat(referencia.getFirst().chars()).containsOnly(0);
            }
        }

        @Test
        void deveRejeitarPepperForaDoCicloDeVida() {
            final var pepper = mock();
            pepper.close();

            assertThatThrownBy(() -> pepper.usar(_ -> null))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Pepper usado após fim do seu ciclo de vida");
        }

        @Test
        void naoDevePermitirAlteracaoNoValor() {
            try (final var pepper = mock()) {
                pepper.usar(segredo -> {
                    segredo.clear();
                    return null;
                });
                pepper.usar(segredo -> assertThat(segredo.chars()).anyMatch(representacao -> representacao != 0));
            }
        }
    }

    private static Pepper mock() {
        final var valor = new byte[COMPRIMENTO];
        Arrays.fill(valor, (byte) -1);
        return new Pepper(valor);
    }
}
