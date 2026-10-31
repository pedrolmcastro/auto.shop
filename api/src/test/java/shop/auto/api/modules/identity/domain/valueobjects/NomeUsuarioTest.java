package shop.auto.api.modules.identity.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario.COMPRIMENTO_MAX;
import static shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario.COMPRIMENTO_MIN;

public final class NomeUsuarioTest {
    @ParameterizedTest
    @ValueSource(strings = { "A", "Z", "0", "9", " ", "_", "-", ".", "á", "ç", "Ω", "ａ", "😀", "\n", "\0" })
    void deveRejeitarCodePointInvalido(final String caractere) {
        final var valor = "a".repeat(COMPRIMENTO_MIN - 1) + caractere;
        assertThatThrownBy(() -> new NomeUsuario(valor))
            .isExactlyInstanceOf(NomeUsuario.InvarianteException.CodePointsInvalidos.class)
            .hasMessage("Nome do usuário deve conter apenas letras ASCII minúsculas mas contém [%s]", caractere);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, COMPRIMENTO_MIN - 1 })
    void deveRejeitarNomeMuitoCurto(final int comprimento) {
        final var valor = "a".repeat(comprimento);
        assertThatThrownBy(() -> new NomeUsuario(valor))
            .isExactlyInstanceOf(NomeUsuario.InvarianteException.Comprimento.MuitoCurto.class)
            .hasMessage("Nome do usuário deve ter no mínimo 3 caracteres mas tem %d", comprimento);
    }

    @Test
    void deveRejeitarNomeMuitoLongo() {
        final var comprimento = COMPRIMENTO_MAX + 1;
        final var valor = "a".repeat(comprimento);
        assertThatThrownBy(() -> new NomeUsuario(valor))
            .isExactlyInstanceOf(NomeUsuario.InvarianteException.Comprimento.MuitoLongo.class)
            .hasMessage("Nome do usuário deve ter no máximo 30 caracteres mas tem %d", comprimento);
    }

    @ParameterizedTest
    @ValueSource(ints = { COMPRIMENTO_MIN, COMPRIMENTO_MIN + (COMPRIMENTO_MAX - COMPRIMENTO_MIN) / 2, COMPRIMENTO_MAX })
    void deveAceitarNomeValido(final int comprimento) {
        final var valor = "a".repeat(comprimento);
        assertThat(new NomeUsuario(valor).valor()).isEqualTo(valor);
    }
}
