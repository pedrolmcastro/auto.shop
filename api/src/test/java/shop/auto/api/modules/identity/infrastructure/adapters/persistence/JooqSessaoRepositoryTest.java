package shop.auto.api.modules.identity.infrastructure.adapters.persistence;

import java.sql.SQLException;
import org.jooq.DSLContext;
import org.jooq.TransactionalCallable;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.dao.DataIntegrityViolationException;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static shop.auto.api.Fixtures.refreshTokenHash;

public final class JooqSessaoRepositoryTest {
    private static DataIntegrityViolationException falha(final @Nullable String estadoSql) {
        return estadoSql == null
            ? new DataIntegrityViolationException("Falha sem causa SQL")
            : new DataIntegrityViolationException("Falha de constraint", new SQLException("", estadoSql));
    }

    private static JooqSessaoRepository repositorioComFalha(final DataIntegrityViolationException excecao) {
        final var dsl = mock(DSLContext.class);
        when(dsl.transactionResult(ArgumentMatchers.<TransactionalCallable<Object>>any())).thenThrow(excecao);
        return new JooqSessaoRepository(dsl);
    }

    @Nested
    final class Inserir {
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "23514" })
        void devePropagarErroNaoTratado(final @Nullable String estadoSql) {
            final var excecao = falha(estadoSql);
            final var sessao = Sessao.criar(UUIDv7.criar(), refreshTokenHash());

            assertThatThrownBy(() -> repositorioComFalha(excecao).inserir(sessao)).isSameAs(excecao);
        }
    }

    @Nested
    final class RotacionarRefreshToken {
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "23514" })
        void devePropagarErroNaoTratado(final @Nullable String estadoSql) {
            final var excecao = falha(estadoSql);

            assertThatThrownBy(
                () -> repositorioComFalha(excecao)
                    .rotacionarRefreshToken(UUIDv7.criar(), refreshTokenHash(), refreshTokenHash())
            ).isSameAs(excecao);
        }
    }
}
