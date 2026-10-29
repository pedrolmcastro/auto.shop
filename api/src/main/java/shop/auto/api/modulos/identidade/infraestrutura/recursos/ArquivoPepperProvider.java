package shop.auto.api.modulos.identidade.infraestrutura.recursos;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import shop.auto.api.modulos.identidade.aplicacao.portas.PepperProvider;
import shop.auto.api.modulos.identidade.dominio.recursos.Pepper;

public final class ArquivoPepperProvider implements PepperProvider {
    private final Path caminho;

    public ArquivoPepperProvider(final Path caminho) {
        this.caminho = caminho;
    }

    @Override
    public Pepper obter() {
        final var bytes = new byte[Pepper.COMPRIMENTO + 1];

        try { // NOSONAR java:S2093
            final int comprimento;

            try (final var arquivo = Files.newInputStream(this.caminho)) {
                comprimento = arquivo.readNBytes(bytes, 0, bytes.length);
            }

            return instanciar(bytes, comprimento);
        } catch (final IOException excecao) {
            throw new FalhaDeLeituraException(excecao);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    private static Pepper instanciar(final byte[] bytes, final int comprimento) {
        final var valor = Arrays.copyOf(bytes, comprimento);

        try { // NOSONAR java:S2093
            return new Pepper(valor);
        } finally {
            Arrays.fill(valor, (byte) 0);
        }
    }

    public static final class FalhaDeLeituraException extends UncheckedIOException {
        private FalhaDeLeituraException(final IOException causa) {
            super("Não foi possível ler o arquivo de pepper", causa);
        }
    }
}
