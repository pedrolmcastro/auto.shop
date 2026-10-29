package shop.auto.api.modules.identity.domain.valueobjects;

import java.util.Arrays;
import java.util.stream.Collectors;
import shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario.InvalidoException.CodigosDePontoInvalidosException;
import shop.auto.api.shared.utils.Ascii;

public record NomeUsuario(String valor) {
    public static final int COMPRIMENTO_MAX = 30;
    public static final int COMPRIMENTO_MIN = 3;

    public NomeUsuario {
        final var codigosDePontoInvalidos = valor
            .codePoints()
            .filter(pontoDeCodigo -> !Ascii.eLetraMinuscula(pontoDeCodigo))
            .distinct()
            .toArray();

        if (codigosDePontoInvalidos.length > 0) {
            throw new CodigosDePontoInvalidosException(codigosDePontoInvalidos);
        }

        final var comprimento = valor.length();

        if (comprimento < COMPRIMENTO_MIN) {
            throw new InvalidoException.MuitoCurtoException(comprimento);
        }

        if (comprimento > COMPRIMENTO_MAX) {
            throw new InvalidoException.MuitoLongoException(comprimento);
        }
    }

    public abstract static sealed class InvalidoException extends IllegalArgumentException {
        public static final class CodigosDePontoInvalidosException extends InvalidoException {
            private CodigosDePontoInvalidosException(final int[] codigosDePontoInvalidos) {
                super(
                    String
                        .format(
                            "Nome do usuário deve conter apenas letras ASCII minúsculas mas contém %s",
                            Arrays
                                .stream(codigosDePontoInvalidos)
                                .sorted()
                                .mapToObj(Character::toString)
                                .collect(Collectors.joining(", ", "[", "]"))
                        )
                );
            }
        }

        public static final class MuitoCurtoException extends InvalidoException {
            private MuitoCurtoException(final int comprimento) {
                super(
                    String
                        .format(
                            "Nome do usuário deve ter no mínimo %d caracteres mas tem %d",
                            COMPRIMENTO_MIN,
                            comprimento
                        )
                );
            }
        }

        public static final class MuitoLongoException extends InvalidoException {
            private MuitoLongoException(final int comprimento) {
                super(
                    String
                        .format(
                            "Nome do usuário deve ter no máximo %d caracteres mas tem %d",
                            COMPRIMENTO_MAX,
                            comprimento
                        )
                );
            }
        }

        private InvalidoException(final String mensagem) {
            super(mensagem);
        }
    }
}
