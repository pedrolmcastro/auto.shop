package shop.auto.api.modules.identity.infrastructure.adapters.persistence;

import java.time.Instant;
import java.time.InstantSource;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import shop.auto.api.IntegrationConfig;
import shop.auto.api.IntegrationFixtures;
import shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.Sessao.SESSAO;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@Import(value = IntegrationConfig.class)
@Transactional
class JooqSessaoRepositoryIT {
    @Test
    void deveInserirSessao(
        @Autowired final DSLContext dsl,
        @Autowired final SessaoRepository repositorio,
        @Autowired final IntegrationFixtures fixtures
    ) {
        final var id = UUIDv7.criar();
        final var usuario = fixtures.admin().id();
        final var criadaEm = Instant.parse("2026-01-01T12:00:00.123456Z");
        final var expiraEm = criadaEm.plus(Sessao.DURACAO);

        final var sessao = new Sessao(id, usuario, criadaEm, expiraEm, null, InstantSource.fixed(criadaEm));
        assertThat(repositorio.inserir(sessao)).isEmpty();

        final var registro = dsl.selectFrom(SESSAO).where(SESSAO.ID.eq(id.valor())).fetchSingle();
        assertThat(registro.getId()).isEqualTo(id.valor());
        assertThat(registro.getUsuario()).isEqualTo(usuario.valor());
        assertThat(registro.getCriadaEm().toInstant()).isEqualTo(criadaEm);
        assertThat(registro.getExpiraEm().toInstant()).isEqualTo(expiraEm);
        assertThat(registro.getRevogadaEm()).isNull();
    }

    @Test
    void deveInserirSessaoRevogada(
        @Autowired final DSLContext dsl,
        @Autowired final SessaoRepository repositorio,
        @Autowired final IntegrationFixtures fixtures
    ) {
        final var id = UUIDv7.criar();
        final var usuario = fixtures.admin().id();
        final var criadaEm = Instant.parse("2026-01-01T12:00:00.123456Z");
        final var expiraEm = criadaEm.plus(Sessao.DURACAO);
        final var revogadaEm = criadaEm;

        final var sessao = new Sessao(id, usuario, criadaEm, expiraEm, revogadaEm, InstantSource.fixed(criadaEm));
        assertThat(repositorio.inserir(sessao)).isEmpty();

        final var registro = dsl.selectFrom(SESSAO).where(SESSAO.ID.eq(id.valor())).fetchSingle();
        assertThat(registro.getId()).isEqualTo(id.valor());
        assertThat(registro.getUsuario()).isEqualTo(usuario.valor());
        assertThat(registro.getCriadaEm().toInstant()).isEqualTo(criadaEm);
        assertThat(registro.getExpiraEm().toInstant()).isEqualTo(expiraEm);
        assertThat(registro.getRevogadaEm().toInstant()).isEqualTo(revogadaEm);
    }

    @Test
    void deveRejeitarIdDuplicado(
        @Autowired final DSLContext dsl,
        @Autowired final SessaoRepository repositorio,
        @Autowired final IntegrationFixtures fixtures
    ) {
        final var sessao = Sessao.criar(fixtures.admin().id());
        assertThat(repositorio.inserir(sessao)).isEmpty();

        final var original = dsl.selectFrom(SESSAO).where(SESSAO.ID.eq(sessao.id().valor())).fetchSingle();

        assertThat(repositorio.inserir(sessao)).contains(new SessaoRepository.ErroInsercao.IdDuplicado(sessao.id()));
        assertThat(dsl.selectFrom(SESSAO).where(SESSAO.ID.eq(sessao.id().valor())).fetchSingle()).isEqualTo(original);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveRejeitarUsuarioInexistente(
        @Autowired final DSLContext dsl,
        @Autowired final SessaoRepository repositorio
    ) {
        final var sessao = Sessao.criar(UUIDv7.criar());

        assertThat(repositorio.inserir(sessao))
            .contains(new SessaoRepository.ErroInsercao.UsuarioInexistente(sessao.usuario()));

        assertThat(dsl.fetchExists(SESSAO, SESSAO.ID.eq(sessao.id().valor()))).isFalse();
    }
}
