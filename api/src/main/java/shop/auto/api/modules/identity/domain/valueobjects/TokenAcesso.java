package shop.auto.api.modules.identity.domain.valueobjects;

import com.google.errorprone.annotations.CheckReturnValue;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTypeValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;

public record TokenAcesso(Jwt jwt) {
    public static final SignatureAlgorithm ALGORITMO = SignatureAlgorithm.RS256;
    public static final String AUDIENCIA = "api.auto.shop";
    public static final String EMISSOR = "auto.shop";

    private static final String[] TIPOS = { "at+jwt", "application/at+jwt", };

    private static final JwtAudienceValidator VALIDADOR_AUDIENCIA = new JwtAudienceValidator(AUDIENCIA);
    private static final JwtIssuerValidator VALIDADOR_EMISSOR = new JwtIssuerValidator(EMISSOR);
    private static final JwtTypeValidator VALIDADOR_TIPO = new JwtTypeValidator(TIPOS);

    public TokenAcesso {
        final var algoritmo = jwt.getHeaders().get("alg");

        if (algoritmo == null) {
            throw new InvalidoException.Algoritmo.Ausente();
        }

        if (ALGORITMO != algoritmo && !ALGORITMO.getName().equals(algoritmo)) {
            throw new InvalidoException.Algoritmo.Invalido(algoritmo);
        }

        final var errosAudiencia = VALIDADOR_AUDIENCIA.validate(jwt).getErrors();

        if (!errosAudiencia.isEmpty()) {
            throw new InvalidoException.AudienciaAusente(errosAudiencia);
        }

        final var errosEmissor = VALIDADOR_EMISSOR.validate(jwt).getErrors();

        if (!errosEmissor.isEmpty()) {
            throw new InvalidoException.EmissorInvalido(errosEmissor);
        }

        final var emitidoEm = emitidoEm(jwt);
        final var expiraEm = expiraEm(jwt);

        if (!expiraEm.isAfter(emitidoEm)) {
            throw new InvalidoException.Expiracao.Invalida(emitidoEm, expiraEm);
        }

        sujeito(jwt);

        final var errosTipo = VALIDADOR_TIPO.validate(jwt).getErrors();

        if (!errosTipo.isEmpty()) {
            throw new InvalidoException.TipoInvalido(errosTipo);
        }
    }

    @CheckReturnValue
    public Instant emitidoEm() {
        return emitidoEm(this.jwt);
    }

    @CheckReturnValue
    private static Instant emitidoEm(final Jwt jwt) {
        final var emitidoEm = jwt.getIssuedAt();

        if (emitidoEm == null) {
            throw new InvalidoException.EmissaoAusente();
        }

        return emitidoEm;
    }

    @CheckReturnValue
    public Instant expiraEm() {
        return expiraEm(this.jwt);
    }

    @CheckReturnValue
    private static Instant expiraEm(final Jwt jwt) {
        final var expiraEm = jwt.getExpiresAt();

        if (expiraEm == null) {
            throw new InvalidoException.Expiracao.Ausente();
        }

        return expiraEm;
    }

    public UUIDv7 sujeito() {
        return sujeito(this.jwt);
    }

    private static UUIDv7 sujeito(final Jwt jwt) {
        final var sujeito = jwt.getSubject();

        if (sujeito == null) {
            throw new InvalidoException.Sujeito.Ausente();
        }

        try {
            return new UUIDv7(UUID.fromString(sujeito));
        } catch (IllegalArgumentException excecao) {
            throw new InvalidoException.Sujeito.Invalido(sujeito, excecao);
        }
    }

    @CheckReturnValue
    public static String tipo() {
        return TIPOS[0];
    }

    @CheckReturnValue
    @Override
    public String toString() {
        return String.format("TokenAcesso[valor=*, emitidoEm=%s, expiraEm=%s]", emitidoEm(), expiraEm());
    }

    @CheckReturnValue
    public String valor() {
        return this.jwt.getTokenValue();
    }

    public abstract static sealed class InvalidoException extends IllegalArgumentException {
        public abstract static sealed class Algoritmo extends InvalidoException {
            public static final class Ausente extends Algoritmo {
                private Ausente() {
                    super("Token de acesso deve conter alg");
                }
            }

            public static final class Invalido extends Algoritmo {
                private Invalido(final Object valor) {
                    super(String.format("alg do token de acesso deve ser %s mas é %s", ALGORITMO.getName(), valor));
                }
            }

            private Algoritmo(final String mensagem) {
                super(mensagem);
            }
        }

        public static final class AudienciaAusente extends InvalidoException {
            private AudienciaAusente(final Collection<OAuth2Error> erros) {
                super(
                    String.format("Audiência do token de acesso deve incluir %s", AUDIENCIA),
                    new JwtValidationException("Falha na validação da audiência do token de acesso", erros)
                );
            }
        }

        public static final class EmissaoAusente extends InvalidoException {
            private EmissaoAusente() {
                super("Token de acesso deve conter momento de emissão");
            }
        }

        public static final class EmissorInvalido extends InvalidoException {
            private EmissorInvalido(final Collection<OAuth2Error> erros) {
                super(
                    String.format("Emissor do token de acesso deve ser %s", EMISSOR),
                    new JwtValidationException("Falha na validação do emissor do token de acesso", erros)
                );
            }
        }

        public abstract static sealed class Expiracao extends InvalidoException {
            public static final class Ausente extends Expiracao {
                private Ausente() {
                    super("Token de acesso deve conter momento de expiração");
                }
            }

            public static final class Invalida extends Expiracao {
                private Invalida(final Instant emitidoEm, final Instant expiraEm) {
                    super(
                        String
                            .format(
                                "Expiração do token de acesso deve ser posterior à %s mas é %s",
                                emitidoEm,
                                expiraEm
                            )
                    );
                }
            }

            private Expiracao(final String mensagem) {
                super(mensagem);
            }
        }

        public abstract static sealed class Sujeito extends InvalidoException {
            public static final class Ausente extends Sujeito {
                private Ausente() {
                    super("Token de acesso deve conter sujeito");
                }
            }

            public static final class Invalido extends Sujeito {
                private Invalido(final String sujeito, final IllegalArgumentException causa) {
                    super(String.format("Sujeito do token de acesso deve ser um UUID mas é %s", sujeito), causa);
                }
            }

            private Sujeito(final String mensagem) {
                super(mensagem);
            }

            private Sujeito(final String mensagem, final IllegalArgumentException causa) {
                super(mensagem, causa);
            }
        }

        public static final class TipoInvalido extends InvalidoException {
            private TipoInvalido(final Collection<OAuth2Error> erros) {
                super(
                    String.format("Tipo do token de acesso deve ser %s", String.join(" ou ", TIPOS)),
                    new JwtValidationException("Falha na validação do tipo do token de acesso", erros)
                );
            }
        }

        private InvalidoException(final String mensagem) {
            super(mensagem);
        }

        private InvalidoException(final String mensagem, final Throwable causa) {
            super(mensagem, causa);
        }
    }
}
