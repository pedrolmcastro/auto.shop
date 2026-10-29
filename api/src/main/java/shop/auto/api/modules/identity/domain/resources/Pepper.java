package shop.auto.api.modules.identity.domain.resources;

import com.password4j.SecureString;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;

public final class Pepper implements AutoCloseable {
    public static final int COMPRIMENTO = 32;
    private static final HexFormat FORMATADOR = HexFormat.of();

    private byte @Nullable [] valor;

    public Pepper(final byte[] valor) {
        if (valor.length != COMPRIMENTO) {
            throw new ComprimentoInvalidoException(valor.length);
        }

        this.valor = valor.clone();
    }

    @Override
    public synchronized void close() {
        if (this.valor != null) {
            Arrays.fill(this.valor, (byte) 0);
            this.valor = null;
        }
    }

    @Override
    public String toString() {
        return "Pepper[valor=*]";
    }

    public <T> T usar(final Function<SecureString, T> operacao) {
        final var copia = codificar();

        try {
            return operacao.apply(copia);
        } finally {
            copia.clear();
        }
    }

    private synchronized SecureString codificar() {
        final var bytes = this.valor;

        if (bytes == null) {
            throw new IllegalStateException("Pepper usado após fim do seu ciclo de vida");
        }

        final var buffer = CharBuffer.allocate(COMPRIMENTO * 2);

        IntStream
            .range(0, COMPRIMENTO)
            .map(i -> bytes[i])
            .flatMap(digito -> IntStream.of(FORMATADOR.toHighHexDigit(digito), FORMATADOR.toLowHexDigit(digito)))
            .forEachOrdered(caractere -> buffer.put((char) caractere));

        final var codificado = buffer.array();

        try {
            return new SecureString(codificado);
        } finally {
            Arrays.fill(codificado, '\0');
        }
    }

    public static final class ComprimentoInvalidoException extends IllegalArgumentException {
        private ComprimentoInvalidoException(final int comprimento) {
            super(String.format("Pepper deve ter exatamente %d bytes mas tem %d", COMPRIMENTO, comprimento));
        }
    }
}
