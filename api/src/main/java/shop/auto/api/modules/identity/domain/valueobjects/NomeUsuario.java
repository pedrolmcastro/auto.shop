package shop.auto.api.modules.identity.domain.valueobjects;

import java.util.Arrays;
import java.util.stream.Collectors;
import shop.auto.api.shared.utils.Ascii;

public record NomeUsuario(String valor) {
    public static final int COMPRIMENTO_MAX = 30;
    public static final int COMPRIMENTO_MIN = 3;

    public NomeUsuario {
        final var codePointsInvalidos = valor
            .codePoints()
            .filter(codePoints -> !Ascii.eLetraMinuscula(codePoints))
            .distinct()
            .toArray();

        if (codePointsInvalidos.length > 0) {
            throw new InvarianteException.CodePointsInvalidos(codePointsInvalidos);
        }

        final var comprimento = valor.length();

        if (comprimento < COMPRIMENTO_MIN) {
            throw new InvarianteException.Comprimento.MuitoCurto(comprimento);
        }

        if (comprimento > COMPRIMENTO_MAX) {
            throw new InvarianteException.Comprimento.MuitoLongo(comprimento);
        }
    }

    public abstract static sealed class InvarianteException extends IllegalArgumentException {
        public static final class CodePointsInvalidos extends InvarianteException {
            private CodePointsInvalidos(final int[] codePointsInvalidos) {
                super(
                    String
                        .format(
                            "Nome do usuário deve conter apenas letras ASCII minúsculas mas contém %s",
                            Arrays
                                .stream(codePointsInvalidos)
                                .sorted()
                                .mapToObj(Character::toString)
                                .collect(Collectors.joining(", ", "[", "]"))
                        )
                );
            }
        }

        public abstract static sealed class Comprimento extends InvarianteException {
            public static final class MuitoCurto extends Comprimento {
                private MuitoCurto(final int comprimento) {
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

            public static final class MuitoLongo extends Comprimento {
                private MuitoLongo(final int comprimento) {
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

            private Comprimento(final String mensagem) {
                super(mensagem);
            }
        }

        private InvarianteException(final String mensagem) {
            super(mensagem);
        }
    }
}
