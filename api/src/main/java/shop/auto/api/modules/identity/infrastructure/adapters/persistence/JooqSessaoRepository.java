package shop.auto.api.modules.identity.infrastructure.adapters.persistence;

import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.Sessao.SESSAO;

@Repository
public class JooqSessaoRepository implements SessaoRepository {
    private final DSLContext dsl;

    public JooqSessaoRepository(final DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<ErroInsercao> inserir(final Sessao sessao) {
        final var estado = sessao.estado();

        final var revogadaEm = estado instanceof Sessao.Estado.Revogada(final var instante)
            ? instante.atOffset(ZoneOffset.UTC)
            : null;

        final int inseridas;

        try {
            inseridas = this.dsl
                .transactionResult(
                    contexto -> contexto
                        .dsl()
                        .insertInto(SESSAO)
                        .set(SESSAO.ID, sessao.id().valor())
                        .set(SESSAO.USUARIO, sessao.usuario().valor())
                        .set(SESSAO.CRIADA_EM, sessao.criadaEm().atOffset(ZoneOffset.UTC))
                        .set(SESSAO.EXPIRA_EM, sessao.expiraEm().atOffset(ZoneOffset.UTC))
                        .set(SESSAO.REVOGADA_EM, revogadaEm)
                        .onConflict(SESSAO.ID)
                        .doNothing()
                        .execute()
                );
        } catch (final DataIntegrityViolationException excecao) {
            if (excecao.getMostSpecificCause() instanceof SQLException sql && "23503".equals(sql.getSQLState())) {
                return Optional.of(new ErroInsercao.UsuarioInexistente(sessao.usuario()));
            }

            throw excecao;
        }

        if (inseridas == 0) {
            return Optional.of(new ErroInsercao.IdDuplicado(sessao.id()));
        }

        return Optional.empty();
    }
}
