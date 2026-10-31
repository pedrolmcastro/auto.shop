package shop.auto.api.modules.identity.domain.valueobjects;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import shop.auto.api.modules.identity.domain.valueobjects.TokenAcesso.InvalidoException;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public final class TokenAcessoTest {
    private static final String VALOR = "token-serializado";
    private static final Instant EMITIDO_EM = Instant.parse("2026-10-30T12:00:00Z");
    private static final Instant EXPIRA_EM = EMITIDO_EM.plusSeconds(300);
    private static final UUIDv7 SUJEITO = UUIDv7.criar();

    @Nested
    public final class Construtor {
        @Test
        void deveAceitarAlgoritmoEnum() {
            final var jwt = jwt().header("alg", TokenAcesso.ALGORITMO).build();
            assertThat(new TokenAcesso(jwt).jwt()).isEqualTo(jwt);
        }

        @Test
        void deveAceitarAlgoritmoTexto() {
            final var jwt = jwt().header("alg", TokenAcesso.ALGORITMO.name()).build();
            assertThat(new TokenAcesso(jwt).jwt()).isEqualTo(jwt);
        }

        @Test
        void deveAceitarMultiplasAudiencias() {
            final var jwt = jwt().claim("aud", List.of("outra.api", TokenAcesso.AUDIENCIA)).build();
            assertThat(new TokenAcesso(jwt).jwt()).isEqualTo(jwt);
        }

        @Test
        void deveAceitarTipoApplicationAtJwt() {
            final var jwt = jwt().header("typ", "application/at+jwt").build();
            final var token = new TokenAcesso(jwt);

            assertThat(token.jwt()).isEqualTo(jwt);
        }

        @Test
        void deveAceitarTipoAtJwt() {
            final var jwt = jwt().header("typ", "at+jwt").build();
            final var token = new TokenAcesso(jwt);

            assertThat(token.jwt()).isEqualTo(jwt);
            assertThat(token.valor()).isEqualTo(VALOR);
            assertThat(token.emitidoEm()).isEqualTo(EMITIDO_EM);
            assertThat(token.expiraEm()).isEqualTo(EXPIRA_EM);
            assertThat(token.sujeito()).isEqualTo(SUJEITO);
        }

        @Test
        void deveRejeitarAlgoritmoAusente() {
            final var jwt = jwt().headers(headers -> headers.remove("alg")).build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.Algoritmo.Ausente.class)
                .hasMessage("Token de acesso deve conter alg");
        }

        @ParameterizedTest
        @EnumSource(value = SignatureAlgorithm.class, mode = EnumSource.Mode.EXCLUDE, names = "RS256")
        void deveRejeitarAlgoritmoInvalido(final SignatureAlgorithm algoritmo) {
            final var jwt = jwt().header("alg", algoritmo).build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.Algoritmo.Invalido.class)
                .hasMessage("alg do token de acesso deve ser %s mas é %s", TokenAcesso.ALGORITMO.getName(), algoritmo);
        }

        @Test
        void deveRejeitarAudienciaAusente() {
            final var jwt = jwt().claims(claims -> claims.remove("aud")).build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.AudienciaAusente.class)
                .hasMessage("Audiência do token de acesso deve incluir %s", TokenAcesso.AUDIENCIA)
                .hasCauseInstanceOf(JwtValidationException.class);
        }

        @Test
        void deveRejeitarEmissaoAusente() {
            final var jwt = jwt().claims(claims -> claims.remove("iat")).build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.EmissaoAusente.class)
                .hasMessage("Token de acesso deve conter momento de emissão");
        }

        @Test
        void deveRejeitarEmissorAusente() {
            final var jwt = jwt().claims(claims -> claims.remove("iss")).build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.EmissorInvalido.class)
                .hasMessage("Emissor do token de acesso deve ser %s", TokenAcesso.EMISSOR)
                .hasCauseInstanceOf(JwtValidationException.class);
        }

        @Test
        void deveRejeitarEmissorInvalido() {
            final var jwt = jwt().claim("iss", "outro.emissor").build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.EmissorInvalido.class)
                .hasMessage("Emissor do token de acesso deve ser %s", TokenAcesso.EMISSOR)
                .hasCauseInstanceOf(JwtValidationException.class);
        }

        @Test
        void deveRejeitarExpiracaoAntesDaEmissao() {
            final var jwt = spy(jwt().build());
            final var expiraEm = EMITIDO_EM.minusNanos(1);
            when(jwt.getExpiresAt()).thenReturn(expiraEm);

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.Expiracao.Invalida.class)
                .hasMessage("Expiração do token de acesso deve ser posterior à %s mas é %s", EMITIDO_EM, expiraEm);
        }

        @Test
        void deveRejeitarExpiracaoAusente() {
            final var jwt = jwt().claims(claims -> claims.remove("exp")).build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.Expiracao.Ausente.class)
                .hasMessage("Token de acesso deve conter momento de expiração");
        }

        @Test
        void deveRejeitarExpiracaoNaEmissao() {
            final var jwt = spy(jwt().build());
            when(jwt.getExpiresAt()).thenReturn(EMITIDO_EM);

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.Expiracao.Invalida.class)
                .hasMessage("Expiração do token de acesso deve ser posterior à %s mas é %s", EMITIDO_EM, EMITIDO_EM);
        }

        @Test
        void deveRejeitarSujeitoAusente() {
            final var jwt = jwt().claims(claims -> claims.remove("sub")).build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.Sujeito.Ausente.class)
                .hasMessage("Token de acesso deve conter sujeito");
        }

        @Test
        void deveRejeitarSujeitoInvalido() {
            final var sujeito = "sujeito";
            final var jwt = jwt().subject(sujeito).build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.Sujeito.Invalido.class)
                .hasMessage("Sujeito do token de acesso deve ser um UUID mas é %s", sujeito);
        }

        @Test
        void deveRejeitarTipoInvalido() {
            final var jwt = jwt().header("typ", "JWT").build();

            assertThatThrownBy(() -> new TokenAcesso(jwt))
                .isExactlyInstanceOf(InvalidoException.TipoInvalido.class)
                .hasMessage("Tipo do token de acesso deve ser at+jwt ou application/at+jwt")
                .hasCauseInstanceOf(JwtValidationException.class);
        }
    }

    @Nested
    public final class ToString {
        @Test
        void deveOcultarValor() {
            final var token = new TokenAcesso(jwt().build());

            assertThat(token.toString())
                .isEqualTo(String.format("TokenAcesso[valor=*, emitidoEm=%s, expiraEm=%s]", EMITIDO_EM, EXPIRA_EM))
                .doesNotContain(VALOR);
        }
    }

    private static Jwt.Builder jwt() {
        return Jwt
            .withTokenValue(VALOR)
            .audience(List.of(TokenAcesso.AUDIENCIA))
            .issuer(TokenAcesso.EMISSOR)
            .subject(SUJEITO.toString())
            .header("alg", TokenAcesso.ALGORITMO)
            .header("typ", TokenAcesso.tipo())
            .issuedAt(EMITIDO_EM)
            .expiresAt(EXPIRA_EM);
    }
}
