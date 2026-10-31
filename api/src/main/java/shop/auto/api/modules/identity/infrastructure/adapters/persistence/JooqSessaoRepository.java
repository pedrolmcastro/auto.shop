package shop.auto.api.modules.identity.infrastructure.adapters.persistence;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import shop.auto.api.modules.identity.application.ports.repositories.SessaoRepository;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.modules.identity.domain.valueobjects.RefreshTokenHash;
import shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.records.SessaoRecord;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.jooq.impl.DSL.selectOne;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.Sessao.SESSAO;
import static shop.auto.api.modules.identity.infrastructure.generated.jooq.tables.SessaoRefreshTokenConsumido.SESSAO_REFRESH_TOKEN_CONSUMIDO;

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
                        .set(SESSAO.REFRESH_TOKEN_HASH, sessao.refreshTokenHash().valor())
                        .set(SESSAO.CRIADA_EM, sessao.criadaEm().atOffset(ZoneOffset.UTC))
                        .set(SESSAO.EXPIRA_EM, sessao.expiraEm().atOffset(ZoneOffset.UTC))
                        .set(SESSAO.REVOGADA_EM, revogadaEm)
                        .onConflict(SESSAO.ID)
                        .doNothing()
                        .execute()
                );
        } catch (final DataIntegrityViolationException excecao) {
            return switch (estadoSql(excecao)) {
                case "23503" -> Optional.of(new ErroInsercao.UsuarioInexistente(sessao.usuario()));
                case "23505" -> Optional.of(new ErroInsercao.RefreshTokenDuplicado(sessao.refreshTokenHash()));
                case null, default -> throw excecao;
            };
        }

        if (inseridas == 0) {
            return Optional.of(new ErroInsercao.IdDuplicado(sessao.id()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<Sessao> buscar(final RefreshTokenHash hash) {
        return this.dsl
            .selectFrom(SESSAO)
            .where(
                SESSAO.REFRESH_TOKEN_HASH
                    .eq(hash.valor())
                    .orExists(
                        selectOne()
                            .from(SESSAO_REFRESH_TOKEN_CONSUMIDO)
                            .where(SESSAO_REFRESH_TOKEN_CONSUMIDO.SESSAO.eq(SESSAO.ID))
                            .and(SESSAO_REFRESH_TOKEN_CONSUMIDO.HASH.eq(hash.valor()))
                    )
            )
            .fetchOptional(JooqSessaoRepository::reconstruir);
    }

    @Override
    public ResultadoRotacao rotacionarRefreshToken(
        final UUIDv7 sessao,
        final RefreshTokenHash atual,
        final RefreshTokenHash novo
    ) {
        try {
            return this.dsl.transactionResult(contexto -> {
                final var transacao = contexto.dsl();
                final var registro = transacao
                    .selectFrom(SESSAO)
                    .where(SESSAO.ID.eq(sessao.valor()))
                    .forUpdate()
                    .fetchOne();

                if (registro == null) {
                    return ResultadoRotacao.Erro.SESSAO_INEXISTENTE;
                }

                final var agora = Instant.now().truncatedTo(ChronoUnit.MICROS).atOffset(ZoneOffset.UTC);

                if (registro.getRevogadaEm() != null || !registro.getExpiraEm().isAfter(agora)) {
                    return ResultadoRotacao.Erro.SESSAO_INATIVA;
                }

                if (!registro.getRefreshTokenHash().equals(atual.valor())) {
                    return ResultadoRotacao.Erro.REFRESH_TOKEN_DIVERGENTE;
                }

                if (
                    atual.equals(novo)
                    || transacao
                        .fetchExists(
                            SESSAO_REFRESH_TOKEN_CONSUMIDO,
                            SESSAO_REFRESH_TOKEN_CONSUMIDO.HASH.eq(novo.valor())
                        )
                ) {
                    return ResultadoRotacao.Erro.REFRESH_TOKEN_DUPLICADO;
                }

                transacao
                    .insertInto(SESSAO_REFRESH_TOKEN_CONSUMIDO)
                    .set(SESSAO_REFRESH_TOKEN_CONSUMIDO.HASH, atual.valor())
                    .set(SESSAO_REFRESH_TOKEN_CONSUMIDO.SESSAO, sessao.valor())
                    .set(SESSAO_REFRESH_TOKEN_CONSUMIDO.CONSUMIDO_EM, agora)
                    .execute();

                final var renovada = transacao
                    .update(SESSAO)
                    .set(SESSAO.REFRESH_TOKEN_HASH, novo.valor())
                    .where(SESSAO.ID.eq(sessao.valor()))
                    .returning()
                    .fetchSingle(JooqSessaoRepository::reconstruir);

                return new ResultadoRotacao.Sucesso(renovada);
            });
        } catch (final DataIntegrityViolationException excecao) {
            if ("23505".equals(estadoSql(excecao))) {
                return ResultadoRotacao.Erro.REFRESH_TOKEN_DUPLICADO;
            }

            throw excecao;
        }
    }

    private static @Nullable String estadoSql(final DataIntegrityViolationException excecao) {
        return excecao.getMostSpecificCause() instanceof SQLException sql
            ? sql.getSQLState()
            : null;
    }

    private static Sessao reconstruir(final SessaoRecord registro) {
        final var revogadaEm = registro.getRevogadaEm();

        return new Sessao(
            new UUIDv7(registro.getId()),
            new UUIDv7(registro.getUsuario()),
            new RefreshTokenHash(registro.getRefreshTokenHash()),
            registro.getCriadaEm().toInstant(),
            registro.getExpiraEm().toInstant(),
            revogadaEm == null
                ? null
                : revogadaEm.toInstant()
        );
    }
}
