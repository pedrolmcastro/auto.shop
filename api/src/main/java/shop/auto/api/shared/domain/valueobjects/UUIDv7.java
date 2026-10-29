package shop.auto.api.shared.domain.valueobjects;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.enums.UuidVariant;
import java.util.UUID;
import shop.auto.api.shared.domain.valueobjects.UUIDv7.InvalidoException.VarianteInvalidaException;
import shop.auto.api.shared.domain.valueobjects.UUIDv7.InvalidoException.VersaoInvalidaException;

public record UUIDv7(UUID valor) implements Comparable<UUIDv7> {
    private static final UuidVariant VARIANTE = UuidVariant.VARIANT_STANDARD;
    private static final int VERSAO = 7;

    public UUIDv7 {
        final var variante = valor.variant();

        if (variante != VARIANTE.getValue()) {
            throw new VarianteInvalidaException(variante);
        }

        final var versao = valor.version();

        if (versao != VERSAO) {
            throw new VersaoInvalidaException(versao);
        }
    }

    public static UUIDv7 criar() {
        return new UUIDv7(UuidCreator.getTimeOrderedEpoch());
    }

    @Override
    public int compareTo(final UUIDv7 outro) {
        return this.valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }

    public abstract static sealed class InvalidoException extends IllegalArgumentException {
        public static final class VarianteInvalidaException extends InvalidoException {
            private VarianteInvalidaException(final int variante) {
                super(String.format("UUIDv7 deve ter variante %d mas tem %d", VARIANTE.getValue(), variante));
            }
        }

        public static final class VersaoInvalidaException extends InvalidoException {
            private VersaoInvalidaException(final int versao) {
                super(String.format("UUIDv7 deve ter versão %d mas tem versão %d", VERSAO, versao));
            }
        }

        private InvalidoException(final String mensagem) {
            super(mensagem);
        }
    }
}
