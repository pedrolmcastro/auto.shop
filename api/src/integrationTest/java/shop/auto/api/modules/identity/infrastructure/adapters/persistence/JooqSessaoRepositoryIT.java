package shop.auto.api.modules.identity.infrastructure.adapters.persistence;

import java.time.Instant;
import java.time.InstantSource;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import shop.auto.api.IntegrationConfig;
import shop.auto.api.IntegrationFixtures;
import shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository;
import shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository.ResultadoRotacao;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.modules.identity.domain.valueobjects.RefreshToken;
import shop.auto.api.modules.identity.domain.valueobjects.RefreshTokenHash;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;
import static shop.auto.api.IntegrationFixtures.refreshTokenHash;
import static shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository.ResultadoRotacao.Erro.REFRESH_TOKEN_DIVERGENTE;
import static shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository.ResultadoRotacao.Erro.REFRESH_TOKEN_DUPLICADO;
import static shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository.ResultadoRotacao.Erro.SESSAO_INATIVA;
import static shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository.ResultadoRotacao.Erro.SESSAO_INEXISTENTE;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.Sessao.SESSAO;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.SessaoRefreshTokenConsumido.SESSAO_REFRESH_TOKEN_CONSUMIDO;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@Import(value = IntegrationConfig.class)
@Transactional
class JooqSessaoRepositoryIT {
    private static final Instant CRIADA_EM = Instant.now().truncatedTo(ChronoUnit.MICROS);
    private static final Instant EXPIRA_EM = CRIADA_EM.plus(Sessao.DURACAO);

    @Autowired
    private DSLContext dsl;

    @Autowired
    private SessaoRepository repositorio;

    @Autowired
    private IntegrationFixtures fixtures;

    private Sessao novaSessao(final RefreshTokenHash hash) {
        return new Sessao(
            UUIDv7.criar(),
            this.fixtures.admin().id(),
            hash,
            CRIADA_EM,
            EXPIRA_EM,
            null,
            InstantSource.fixed(CRIADA_EM)
        );
    }

    private int contarConsumos(final Sessao sessao) {
        return this.dsl
            .fetchCount(SESSAO_REFRESH_TOKEN_CONSUMIDO, SESSAO_REFRESH_TOKEN_CONSUMIDO.SESSAO.eq(sessao.id().valor()));
    }

    @Nested
    final class Inserir {
        @Test
        void devePersistirSessaoComHashDoRefresh() {
            final var token = RefreshToken.criar();
            final var sessao = novaSessao(RefreshTokenHash.hash(token));

            assertThat(repositorio.inserir(sessao)).isEmpty();

            assertThat(repositorio.buscar(sessao.refreshTokenHash())).hasValueSatisfying(encontrada -> {
                assertThat(encontrada.id()).isEqualTo(sessao.id());
                assertThat(encontrada.usuario()).isEqualTo(sessao.usuario());
                assertThat(encontrada.refreshTokenHash()).isEqualTo(sessao.refreshTokenHash());
                assertThat(encontrada.criadaEm()).isEqualTo(CRIADA_EM);
                assertThat(encontrada.expiraEm()).isEqualTo(EXPIRA_EM);
                assertThat(encontrada.estado()).isEqualTo(new Sessao.Estado.Ativa(EXPIRA_EM));
            });
        }

        @Test
        void deveRejeitarIdDuplicadoSemAlterarOriginal() {
            final var sessao = novaSessao(refreshTokenHash());
            assertThat(repositorio.inserir(sessao)).isEmpty();

            final var original = dsl.selectFrom(SESSAO).where(SESSAO.ID.eq(sessao.id().valor())).fetchSingle();

            assertThat(repositorio.inserir(sessao))
                .contains(new SessaoRepository.ErroInsercao.IdDuplicado(sessao.id()));
            assertThat(dsl.selectFrom(SESSAO).where(SESSAO.ID.eq(sessao.id().valor())).fetchSingle())
                .isEqualTo(original);
        }

        @Test
        void deveRejeitarRefreshDuplicadoSemAbortarTransacao() {
            final var hash = refreshTokenHash();
            final var original = novaSessao(hash);
            final var duplicada = novaSessao(hash);

            assertThat(repositorio.inserir(original)).isEmpty();
            assertThat(repositorio.inserir(duplicada))
                .contains(new SessaoRepository.ErroInsercao.RefreshTokenDuplicado(hash));
            assertThat(repositorio.buscar(hash)).contains(original);

            assertThat(repositorio.inserir(novaSessao(refreshTokenHash()))).isEmpty();
        }

        @Test
        void deveRejeitarUsuarioInexistenteSemAbortarTransacao() {
            final var sessao = Sessao.criar(UUIDv7.criar(), refreshTokenHash());

            assertThat(repositorio.inserir(sessao))
                .contains(new SessaoRepository.ErroInsercao.UsuarioInexistente(sessao.usuario()));
            assertThat(dsl.fetchExists(SESSAO, SESSAO.ID.eq(sessao.id().valor()))).isFalse();

            assertThat(repositorio.inserir(novaSessao(refreshTokenHash()))).isEmpty();
        }
    }

    @Nested
    final class Buscar {
        @Test
        void deveRetornarVazioQuandoHashNaoExiste() {
            assertThat(repositorio.buscar(refreshTokenHash())).isEmpty();
        }
    }

    @Nested
    final class RotacionarRefreshToken {
        @Test
        void deveRotacionarEImpedirReutilizacao() {
            final var anterior = refreshTokenHash();
            final var novo = refreshTokenHash();
            final var sessao = novaSessao(anterior);
            assertThat(repositorio.inserir(sessao)).isEmpty();
            final var antes = Instant.now().truncatedTo(ChronoUnit.MICROS);

            assertThat(repositorio.rotacionarRefreshToken(sessao.id(), anterior, novo))
                .isInstanceOfSatisfying(ResultadoRotacao.Sucesso.class, sucesso -> {
                    assertThat(sucesso.sessao().id()).isEqualTo(sessao.id());
                    assertThat(sucesso.sessao().refreshTokenHash()).isEqualTo(novo);
                    assertThat(sucesso.sessao().expiraEm()).isEqualTo(EXPIRA_EM);
                });
            assertThat(repositorio.rotacionarRefreshToken(sessao.id(), anterior, refreshTokenHash()))
                .isEqualTo(REFRESH_TOKEN_DIVERGENTE);
            assertThat(repositorio.rotacionarRefreshToken(sessao.id(), novo, anterior))
                .isEqualTo(REFRESH_TOKEN_DUPLICADO);

            assertThat(repositorio.buscar(anterior)).hasValueSatisfying(encontrada -> {
                assertThat(encontrada.refreshTokenHash()).isEqualTo(novo);
                assertThat(encontrada.expiraEm()).isEqualTo(EXPIRA_EM);
            });
            assertThat(repositorio.buscar(novo)).contains(sessao);
            final var consumo = dsl
                .selectFrom(SESSAO_REFRESH_TOKEN_CONSUMIDO)
                .where(SESSAO_REFRESH_TOKEN_CONSUMIDO.SESSAO.eq(sessao.id().valor()))
                .fetchSingle();
            assertThat(consumo.getHash()).isEqualTo(anterior.valor());
            assertThat(consumo.getConsumidoEm().toInstant()).isBetween(antes, Instant.now());
        }

        @Test
        void deveRejeitarSessaoInexistente() {
            final var atual = refreshTokenHash();

            assertThat(repositorio.rotacionarRefreshToken(UUIDv7.criar(), atual, refreshTokenHash()))
                .isEqualTo(SESSAO_INEXISTENTE);
        }

        @ParameterizedTest
        @ValueSource(booleans = { false, true })
        void deveRejeitarSessaoInativaSemConsumirRefresh(final boolean revogada) {
            final var hash = refreshTokenHash();
            final Sessao sessao;

            if (revogada) {
                sessao = novaSessao(hash);
                sessao.revogar();
            } else {
                final var criadaEm = Instant.parse("2026-01-01T12:00:00Z");
                sessao = new Sessao(
                    UUIDv7.criar(),
                    fixtures.admin().id(),
                    hash,
                    criadaEm,
                    criadaEm.plus(Sessao.DURACAO),
                    null,
                    InstantSource.fixed(criadaEm)
                );
            }

            assertThat(repositorio.inserir(sessao)).isEmpty();
            assertThat(repositorio.rotacionarRefreshToken(sessao.id(), hash, refreshTokenHash()))
                .isEqualTo(SESSAO_INATIVA);
            assertThat(repositorio.buscar(hash)).hasValueSatisfying(encontrada -> {
                assertThat(encontrada.refreshTokenHash()).isEqualTo(hash);
                final var esperado = revogada
                    ? new Sessao.Estado.Revogada(CRIADA_EM)
                    : new Sessao.Estado.Expirada(sessao.expiraEm());
                assertThat(encontrada.estado()).isEqualTo(esperado);
            });
            assertThat(contarConsumos(sessao)).isZero();
        }

        @Test
        void deveRejeitarNovoHashIgualAoAtual() {
            final var hash = refreshTokenHash();
            final var sessao = novaSessao(hash);
            assertThat(repositorio.inserir(sessao)).isEmpty();

            assertThat(repositorio.rotacionarRefreshToken(sessao.id(), hash, hash)).isEqualTo(REFRESH_TOKEN_DUPLICADO);
            assertThat(contarConsumos(sessao)).isZero();
        }

        @Test
        void deveDesfazerConsumoQuandoAtualizacaoDoHashFalhar() {
            final var atual = refreshTokenHash();
            final var duplicado = refreshTokenHash();
            final var sessao = novaSessao(atual);
            assertThat(repositorio.inserir(sessao)).isEmpty();
            assertThat(repositorio.inserir(novaSessao(duplicado))).isEmpty();

            assertThat(repositorio.rotacionarRefreshToken(sessao.id(), atual, duplicado))
                .isEqualTo(REFRESH_TOKEN_DUPLICADO);
            assertThat(repositorio.buscar(atual))
                .hasValueSatisfying(encontrada -> assertThat(encontrada.refreshTokenHash()).isEqualTo(atual));
            assertThat(contarConsumos(sessao)).isZero();

            assertThat(repositorio.rotacionarRefreshToken(sessao.id(), atual, refreshTokenHash()))
                .isInstanceOf(ResultadoRotacao.Sucesso.class);
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void devePermitirSomenteUmaRotacaoConcorrente() throws Exception {
            final var atual = refreshTokenHash();
            final var primeiro = refreshTokenHash();
            final var segundo = refreshTokenHash();
            final var sessao = novaSessao(atual);
            assertThat(repositorio.inserir(sessao)).isEmpty();
            final var inicio = new CyclicBarrier(2);

            try (final var executor = Executors.newFixedThreadPool(2)) {
                final var resultados = List.of(primeiro, segundo).stream().map(novo -> executor.submit(() -> {
                    inicio.await(5, TimeUnit.SECONDS);
                    return repositorio.rotacionarRefreshToken(sessao.id(), atual, novo);
                })).toList();

                assertThat(
                    List.of(resultados.get(0).get(10, TimeUnit.SECONDS), resultados.get(1).get(10, TimeUnit.SECONDS))
                )
                    .satisfiesOnlyOnce(resultado -> assertThat(resultado).isInstanceOf(ResultadoRotacao.Sucesso.class))
                    .contains(REFRESH_TOKEN_DIVERGENTE);
                assertThat(repositorio.buscar(atual))
                    .hasValueSatisfying(
                        encontrada -> assertThat(encontrada.refreshTokenHash()).isIn(primeiro, segundo)
                    );
                assertThat(contarConsumos(sessao)).isOne();
            } finally {
                dsl.transaction(contexto -> {
                    contexto
                        .dsl()
                        .deleteFrom(SESSAO_REFRESH_TOKEN_CONSUMIDO)
                        .where(SESSAO_REFRESH_TOKEN_CONSUMIDO.SESSAO.eq(sessao.id().valor()))
                        .execute();
                    contexto.dsl().deleteFrom(SESSAO).where(SESSAO.ID.eq(sessao.id().valor())).execute();
                });
            }
        }
    }
}
