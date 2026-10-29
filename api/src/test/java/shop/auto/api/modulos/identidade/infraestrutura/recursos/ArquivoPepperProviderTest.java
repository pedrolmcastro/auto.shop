package shop.auto.api.modulos.identidade.infraestrutura.recursos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shop.auto.api.modulos.identidade.dominio.recursos.Pepper;
import shop.auto.api.modulos.identidade.infraestrutura.recursos.ArquivoPepperProvider.FalhaDeLeituraException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class ArquivoPepperProviderTest {
    private static final HexFormat FORMATADOR = HexFormat.of();

    @TempDir
    private Path diretorio;

    @Test
    void deveLerArquivo() throws IOException {
        final var bytes = new byte[Pepper.COMPRIMENTO];
        Arrays.fill(bytes, (byte) 1);

        final var provedor = new ArquivoPepperProvider(Files.write(this.diretorio.resolve("pepper"), bytes));

        try (final var pepper = provedor.obter()) {
            pepper.usar(segredo -> assertThat(FORMATADOR.parseHex(segredo)).containsExactly(bytes));
        }
    }

    @Test
    void deveInstanciarPeppersDiferentes() throws IOException {
        final var bytes = new byte[Pepper.COMPRIMENTO];
        Arrays.fill(bytes, (byte) 1);

        final var provedor = new ArquivoPepperProvider(Files.write(this.diretorio.resolve("pepper"), bytes));

        try (final var primeiro = provedor.obter(); final var segundo = provedor.obter()) {
            assertThat(primeiro).isNotSameAs(segundo);

            primeiro.close();
            assertThatCode(() -> segundo.usar(_ -> null)).doesNotThrowAnyException();
        }
    }

    @Test
    void deveRejeitarArquivoInexistente() {
        final var provedor = new ArquivoPepperProvider(this.diretorio.resolve("inexistente"));

        assertThatThrownBy(provedor::obter)
            .isExactlyInstanceOf(FalhaDeLeituraException.class)
            .hasMessage("Não foi possível ler o arquivo de pepper")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void deveRejeitarDiretorio() {
        final var provedor = new ArquivoPepperProvider(this.diretorio);

        assertThatThrownBy(provedor::obter)
            .isExactlyInstanceOf(FalhaDeLeituraException.class)
            .hasMessage("Não foi possível ler o arquivo de pepper")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void deveRelerArquivo() throws IOException {
        final var original = new byte[Pepper.COMPRIMENTO];
        Arrays.fill(original, (byte) 1);

        final var caminho = Files.write(this.diretorio.resolve("pepper"), original);
        final var provedor = new ArquivoPepperProvider(caminho);

        try (final var primeiro = provedor.obter()) {
            final var alterado = new byte[Pepper.COMPRIMENTO];
            Arrays.fill(alterado, (byte) 2);
            Files.write(caminho, alterado);

            try (final var segundo = provedor.obter()) {
                primeiro.usar(segredo -> assertThat(FORMATADOR.parseHex(segredo)).containsExactly(original));
                segundo.usar(segredo -> assertThat(FORMATADOR.parseHex(segredo)).containsExactly(alterado));
            }
        }
    }
}
