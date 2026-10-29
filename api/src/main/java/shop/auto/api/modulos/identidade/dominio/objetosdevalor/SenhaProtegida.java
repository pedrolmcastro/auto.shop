package shop.auto.api.modulos.identidade.dominio.objetosdevalor;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.SecureString;
import com.password4j.types.Argon2;
import java.util.Base64;
import java.util.Locale;
import shop.auto.api.modulos.identidade.dominio.recursos.Pepper;

public final class SenhaProtegida {
    public static final int COMPRIMENTO = 32;
    public static final int COMPRIMENTO_SALT = 16;
    public static final int ITERACOES = 2;
    public static final int MEMORIA = 19 * 1024;
    public static final Argon2 VARIANTE = Argon2.ID;
    public static final int VERSAO = 19;
    public static final int PARALELISMO = 1;

    public static final Argon2Function ARGON2 = Argon2Function
        .getInstance(MEMORIA, ITERACOES, PARALELISMO, COMPRIMENTO, VARIANTE, VERSAO);

    private final String valor;

    private SenhaProtegida(final String valor) {
        this.valor = valor;
    }

    public static SenhaProtegida proteger(final SecureString senha, final Pepper pepper) {
        return new SenhaProtegida(
            pepper.usar(p -> Password.hash(senha).addRandomSalt(COMPRIMENTO_SALT).addPepper(p).with(ARGON2).getResult())
        );
    }

    // @CodeScene(disable:"Complex Method")
    public static SenhaProtegida reconstruir(final String valor) {
        final Argon2Function funcao;

        try {
            funcao = Argon2Function.getInstanceFromHash(valor);
        } catch (final RuntimeException excecao) {
            throw new InvalidaException.FormatoInvalidoException(excecao);
        }

        final var comprimento = funcao.getOutputLength();

        if (comprimento < COMPRIMENTO) {
            throw new InvalidaException.ComprimentoInsuficienteException(comprimento);
        }

        final int comprimentoSalt;

        try {
            comprimentoSalt = Base64.getDecoder().decode(getSalt(valor)).length;
        } catch (final IllegalArgumentException excecao) {
            throw new InvalidaException.SaltInvalidoException.FormatoInvalidoException(excecao);
        }

        if (comprimentoSalt < COMPRIMENTO_SALT) {
            throw new InvalidaException.SaltInvalidoException.ComprimentoInvalidoException(comprimentoSalt);
        }

        final var iteracoes = funcao.getIterations();

        if (iteracoes < ITERACOES) {
            throw new InvalidaException.IteracoesInsuficientesException(iteracoes);
        }

        final var memoria = funcao.getMemory();

        if (memoria < MEMORIA) {
            throw new InvalidaException.MemoriaInsuficienteException(memoria);
        }

        final var paralelismo = funcao.getParallelism();

        if (paralelismo != PARALELISMO) {
            throw new InvalidaException.ParalelismoInvalidoException(paralelismo);
        }

        final var variante = funcao.getVariant();

        if (variante != VARIANTE) {
            throw new InvalidaException.VarianteInvalidaException(variante);
        }

        final var versao = funcao.getVersion();

        if (versao != VERSAO) {
            throw new InvalidaException.VersaoInvalidaException(versao);
        }

        return new SenhaProtegida(valor);
    }

    @Override
    public boolean equals(final Object outra) {
        return outra instanceof SenhaProtegida senha && this.valor.equals(senha.valor);
    }

    @Override
    public int hashCode() {
        return this.valor.hashCode();
    }

    public String salt() {
        return getSalt(this.valor);
    }

    @Override
    public String toString() {
        return "SenhaProtegida[valor=*]";
    }

    public String valor() {
        return this.valor;
    }

    public boolean verificar(final SecureString senha, final Pepper pepper) {
        final var funcao = Argon2Function.getInstanceFromHash(this.valor);
        return pepper.usar(p -> Password.check(senha, this.valor).addPepper(p).with(funcao));
    }

    public abstract static sealed class InvalidaException extends IllegalArgumentException {
        public static final class ComprimentoInsuficienteException extends InvalidaException {
            private ComprimentoInsuficienteException(final int comprimento) {
                super(String.format("Hash da senha deve ter no mínimo %d bytes mas tem %d", COMPRIMENTO, comprimento));
            }
        }

        public static final class FormatoInvalidoException extends InvalidaException {
            private FormatoInvalidoException(final RuntimeException causa) {
                super("Senha deve ser protegida com hash Argon2id", causa);
            }
        }

        public static final class IteracoesInsuficientesException extends InvalidaException {
            private IteracoesInsuficientesException(final int iteracoes) {
                super(String.format("Hash da senha deve usar no mínimo %d iterações mas usa %d", ITERACOES, iteracoes));
            }
        }

        public static final class MemoriaInsuficienteException extends InvalidaException {
            private MemoriaInsuficienteException(final int memoria) {
                super(
                    String.format("Hash da senha deve usar no mínimo %d KiB de memória mas usa %d", MEMORIA, memoria)
                );
            }
        }

        public static final class ParalelismoInvalidoException extends InvalidaException {
            private ParalelismoInvalidoException(final int paralelismo) {
                super(String.format("Hash da senha deve paralelismo %d mas usa %d", PARALELISMO, paralelismo));
            }
        }

        public abstract static sealed class SaltInvalidoException extends InvalidaException {
            public static final class ComprimentoInvalidoException extends SaltInvalidoException {
                private ComprimentoInvalidoException(final int comprimento) {
                    super(
                        String
                            .format(
                                "Salt da senha deve ter no mínimo %d bytes mas tem %d",
                                COMPRIMENTO_SALT,
                                comprimento
                            )
                    );
                }
            }

            public static final class FormatoInvalidoException extends SaltInvalidoException {
                private FormatoInvalidoException(final IllegalArgumentException causa) {
                    super("Salt da senha deve estar codificado em Base64", causa);
                }
            }

            private SaltInvalidoException(final String mensagem) {
                super(mensagem);
            }

            private SaltInvalidoException(final String mensagem, final Throwable causa) {
                super(mensagem, causa);
            }
        }

        public static final class VarianteInvalidaException extends InvalidaException {
            private VarianteInvalidaException(final Argon2 variante) {
                super(
                    String
                        .format(
                            "Hash da senha deve usar a variante Argon2id mas usa Argon2%s",
                            variante.name().toLowerCase(Locale.ROOT)
                        )
                );
            }
        }

        public static final class VersaoInvalidaException extends InvalidaException {
            private VersaoInvalidaException(final int versao) {
                super(String.format("Hash da senha deve usar a versão %d do Argon2 mas usa %d", VERSAO, versao));
            }
        }

        private InvalidaException(final String mensagem) {
            super(mensagem);
        }

        private InvalidaException(final String mensagem, final Throwable causa) {
            super(mensagem, causa);
        }
    }

    private static String getSalt(final String valor) {
        return valor.split("\\$", -1)[4];
    }
}
