package shop.auto.api.modules.identity.domain.entities;

import java.time.Instant;
import java.time.InstantSource;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import shop.auto.api.modules.identity.domain.valueobjects.RefreshTokenHash;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static shop.auto.api.Fixtures.refreshTokenHash;
import static shop.auto.api.modules.identity.domain.entities.Sessao.DURACAO;

public final class SessaoTest {
    private static final RefreshTokenHash REFRESH_TOKEN_HASH = refreshTokenHash();
    private static final UUIDv7 ID = UUIDv7.criar();
    private static final UUIDv7 USUARIO = UUIDv7.criar();
    private static final Instant CRIADA_EM = Instant.parse("2026-10-30T12:00:00Z");
    private static final Instant EXPIRA_EM = CRIADA_EM.plus(DURACAO);
    private static final InstantSource RELOGIO = InstantSource.fixed(CRIADA_EM);

    @Nested
    final class Construtor {
        @Test
        void deveAceitarSessao() {
            final var sessao = new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, null, RELOGIO);

            assertThat(sessao.id()).isEqualTo(ID);
            assertThat(sessao.usuario()).isEqualTo(USUARIO);
            assertThat(sessao.refreshTokenHash()).isEqualTo(REFRESH_TOKEN_HASH);
            assertThat(sessao.criadaEm()).isEqualTo(CRIADA_EM);
            assertThat(sessao.expiraEm()).isEqualTo(EXPIRA_EM);
        }

        @Test
        void deveAceitarRevogacaoNaCriacao() {
            final var sessao = new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, CRIADA_EM, RELOGIO);

            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Revogada(CRIADA_EM));
        }

        @Test
        void deveRejeitarCriacaoDepoisDoRelogio() {
            final var criadaEm = CRIADA_EM.plusNanos(1);

            assertThatThrownBy(() -> new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, criadaEm, EXPIRA_EM, null, RELOGIO))
                .isExactlyInstanceOf(Sessao.InvarianteException.CriacaoInvalida.class)
                .hasMessage("Criação deve ser anterior ao instante atual %s mas é %s", CRIADA_EM, criadaEm);
        }

        @ParameterizedTest
        @ValueSource(longs = { -1, 0 })
        void deveRejeitarExpiracaoNaOuAntesDaCriacao(final long nanosAposCriacao) {
            final var expiraEm = CRIADA_EM.plusNanos(nanosAposCriacao);

            assertThatThrownBy(() -> new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, expiraEm, null, RELOGIO))
                .isExactlyInstanceOf(Sessao.InvarianteException.ExpiracaoInvalida.class)
                .hasMessage("Expiração deve ser posterior à criação %s mas é %s", CRIADA_EM, expiraEm);
        }

        @Test
        void deveRejeitarRevogacaoAntesDaCriacao() {
            final var revogadaEm = CRIADA_EM.minusNanos(1);

            assertThatThrownBy(
                () -> new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, revogadaEm, RELOGIO)
            )
                .isExactlyInstanceOf(Sessao.InvarianteException.Revogacao.AntesDaCriacao.class)
                .hasMessage("Revogação deve ser posterior à criação %s mas é %s", CRIADA_EM, revogadaEm);
        }

        @ParameterizedTest
        @ValueSource(longs = { 0, 1 })
        void deveRejeitarRevogacaoNaOuDepoisDaExpiracao(final long nanosAposExpiracao) {
            final var revogadaEm = EXPIRA_EM.plusNanos(nanosAposExpiracao);
            final var relogio = InstantSource.fixed(revogadaEm);

            assertThatThrownBy(
                () -> new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, revogadaEm, relogio)
            )
                .isExactlyInstanceOf(Sessao.InvarianteException.Revogacao.DepoisDaExpiracao.class)
                .hasMessage("Revogação deve ser anterior à expiração %s mas é %s", EXPIRA_EM, revogadaEm);
        }

        @Test
        void deveUsarRelogioDoSistemaPorPadrao() {
            final var criadaEm = Instant.now();
            final var sessao = new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, criadaEm, criadaEm.plus(DURACAO), null);

            sessao.revogar();

            assertThat(sessao.estado())
                .isInstanceOfSatisfying(
                    Sessao.Estado.Revogada.class,
                    estado -> assertThat(estado.revogadaEm()).isBetween(criadaEm, Instant.now())
                );
        }
    }

    @Nested
    final class Criar {
        @Test
        void deveCriarSessao() {
            final var sessao = Sessao.criar(USUARIO, REFRESH_TOKEN_HASH, RELOGIO);

            assertThat(sessao.id()).isNotEqualTo(Sessao.criar(USUARIO, REFRESH_TOKEN_HASH, RELOGIO).id());
            assertThat(sessao.usuario()).isEqualTo(USUARIO);
            assertThat(sessao.refreshTokenHash()).isEqualTo(REFRESH_TOKEN_HASH);
            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Ativa(EXPIRA_EM));
        }

        @Test
        void deveUsarRelogioDoSistemaPorPadrao() {
            final var antes = Instant.now();
            final var sessao = Sessao.criar(USUARIO, REFRESH_TOKEN_HASH);
            final var depois = Instant.now();

            assertThat(sessao.estado())
                .isInstanceOfSatisfying(
                    Sessao.Estado.Ativa.class,
                    estado -> assertThat(estado.expiraEm()).isBetween(antes.plus(DURACAO), depois.plus(DURACAO))
                );
        }
    }

    @Nested
    final class Estado {
        @Test
        void deveRetornarSessaoAtiva() {
            final var relogio = InstantSource.fixed(EXPIRA_EM.minusNanos(1));
            final var sessao = new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, null, relogio);

            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Ativa(EXPIRA_EM));
        }

        @ParameterizedTest
        @ValueSource(longs = { 0, 1 })
        void deveRetornarSessaoExpirada(final long nanosDepoisDaExpiracao) {
            final var relogio = InstantSource.fixed(EXPIRA_EM.plusNanos(nanosDepoisDaExpiracao));
            final var sessao = new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, null, relogio);

            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Expirada(EXPIRA_EM));
        }

        @Test
        void deveRetornarSessaoRevogada() {
            final var revogadaEm = CRIADA_EM.plusSeconds(1);
            final var relogio = InstantSource.fixed(EXPIRA_EM.plusNanos(1));
            final var sessao = new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, revogadaEm, relogio);

            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Revogada(revogadaEm));
        }

        @Test
        void deveSerBaseadoNoRelogio() {
            final var agora = new AtomicReference<>(CRIADA_EM);
            final var sessao = new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, null, agora::get);

            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Ativa(EXPIRA_EM));
            agora.set(EXPIRA_EM);
            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Expirada(EXPIRA_EM));
        }
    }

    @Nested
    final class Revogar {
        @Test
        void deveRevogarSessaoAtiva() {
            final var agora = new AtomicReference<>(CRIADA_EM);
            final var sessao = Sessao.criar(USUARIO, REFRESH_TOKEN_HASH, agora::get);
            final var revogadaEm = EXPIRA_EM.minusNanos(1);
            agora.set(revogadaEm);

            sessao.revogar();

            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Revogada(revogadaEm));
        }

        @ParameterizedTest
        @ValueSource(longs = { 0, 1 })
        void naoDeveRevogarSessaoExpirada(final long nanosDepoisDaExpiracao) {
            final var relogio = InstantSource.fixed(EXPIRA_EM.plusNanos(nanosDepoisDaExpiracao));
            final var sessao = new Sessao(ID, USUARIO, REFRESH_TOKEN_HASH, CRIADA_EM, EXPIRA_EM, null, relogio);

            sessao.revogar();

            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Expirada(EXPIRA_EM));
        }

        @Test
        void naoDeveRevogarSessaoRevogada() {
            final var agora = new AtomicReference<>(CRIADA_EM);
            final var sessao = Sessao.criar(USUARIO, REFRESH_TOKEN_HASH, agora::get);
            sessao.revogar();
            agora.set(CRIADA_EM.plusSeconds(1));

            sessao.revogar();

            assertThat(sessao.estado()).isEqualTo(new Sessao.Estado.Revogada(CRIADA_EM));
        }
    }
}
