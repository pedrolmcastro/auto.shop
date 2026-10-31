package shop.auto.api.modules.identity.infrastructure.adapters.security;

import java.time.Instant;
import java.time.InstantSource;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import shop.auto.api.modules.identity.application.ports.providers.TokenAcessoProvider.ResultadoEmissao;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.modules.identity.domain.valueobjects.TokenAcesso;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;
import static shop.auto.api.Fixtures.refreshTokenHash;
import static shop.auto.api.modules.identity.infrastructure.adapters.security.NimbusTokenAcessoProvider.DURACAO;

public final class NimbusTokenAcessoProviderTest {
    private static final Instant AGORA = Instant.parse("2026-10-30T12:00:00.123Z");
    private static final InstantSource RELOGIO = InstantSource.fixed(AGORA);

    @Test
    void deveEmitirTokenComExpiraEmDaDuracao() {
        final var sessao = Sessao.criar(UUIDv7.criar(), refreshTokenHash(), RELOGIO);
        final var provider = new NimbusTokenAcessoProvider(encoder(), RELOGIO);
        final var emitidoEm = AGORA.truncatedTo(ChronoUnit.SECONDS);

        assertThat(provider.emitir(sessao)).isInstanceOfSatisfying(ResultadoEmissao.Sucesso.class, sucesso -> {
            final var token = sucesso.token();
            final var jwt = token.jwt();

            assertThat(token.emitidoEm()).isEqualTo(emitidoEm);
            assertThat(token.expiraEm()).isEqualTo(emitidoEm.plus(DURACAO));
            assertThat(jwt.getAudience()).containsExactly(TokenAcesso.AUDIENCIA);
            assertThat(jwt.getClaimAsString("iss")).isEqualTo(TokenAcesso.EMISSOR);
            assertThat(jwt.getHeaders())
                .containsEntry("alg", TokenAcesso.ALGORITMO)
                .containsEntry("typ", TokenAcesso.tipo());
        });
    }

    @Test
    void deveEmitirTokenComExpiraEmDaSessao() {
        final var expiraEm = AGORA.plusSeconds(30);
        final var sessao = new Sessao(
            UUIDv7.criar(),
            UUIDv7.criar(),
            refreshTokenHash(),
            AGORA,
            expiraEm,
            null,
            RELOGIO
        );
        final var provider = new NimbusTokenAcessoProvider(encoder(), RELOGIO);

        assertThat(provider.emitir(sessao))
            .isInstanceOfSatisfying(
                ResultadoEmissao.Sucesso.class,
                sucesso -> assertThat(sucesso.token().expiraEm()).isEqualTo(expiraEm.truncatedTo(ChronoUnit.SECONDS))
            );
    }

    @Test
    void deveRejeitarSessaoExpirada() {
        final var sessao = new Sessao(
            UUIDv7.criar(),
            UUIDv7.criar(),
            refreshTokenHash(),
            AGORA.minusSeconds(30),
            AGORA,
            null,
            RELOGIO
        );
        final var provider = new NimbusTokenAcessoProvider(encoder(), RELOGIO);

        assertThat(provider.emitir(sessao)).isEqualTo(ResultadoEmissao.Erro.SESSAO_INATIVA);
    }

    @Test
    void deveRejeitarSessaoRevogada() {
        final var sessao = Sessao.criar(UUIDv7.criar(), refreshTokenHash(), RELOGIO);
        sessao.revogar();
        final var provider = new NimbusTokenAcessoProvider(encoder(), RELOGIO);

        assertThat(provider.emitir(sessao)).isEqualTo(ResultadoEmissao.Erro.SESSAO_INATIVA);
    }

    @Test
    void deveRejeitarTempoInsuficiente() {
        final var sessao = new Sessao(
            UUIDv7.criar(),
            UUIDv7.criar(),
            refreshTokenHash(),
            AGORA,
            AGORA.plusNanos(1),
            null,
            RELOGIO
        );
        final var provider = new NimbusTokenAcessoProvider(encoder(), RELOGIO);

        assertThat(provider.emitir(sessao)).isEqualTo(ResultadoEmissao.Erro.TEMPO_INSUFICIENTE);
    }

    @Test
    void deveUsarRelogioDoSistemaPorPadrao() {
        final var antes = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final var sessao = Sessao.criar(UUIDv7.criar(), refreshTokenHash());
        final var provider = new NimbusTokenAcessoProvider(encoder());

        assertThat(provider.emitir(sessao))
            .isInstanceOfSatisfying(
                ResultadoEmissao.Sucesso.class,
                sucesso -> assertThat(sucesso.token().emitidoEm()).isBetween(antes, Instant.now())
            );
    }

    private static JwtEncoder encoder() {
        return parametros -> {
            final var claims = parametros.getClaims();

            return new Jwt(
                "token-serializado",
                claims.getIssuedAt(),
                claims.getExpiresAt(),
                Objects.requireNonNull(parametros.getJwsHeader()).getHeaders(),
                claims.getClaims()
            );
        };
    }
}
