package shop.auto.api.modules.identity.infrastructure.adapters.persistence;

import java.sql.SQLException;
import org.jooq.DSLContext;
import org.jooq.TransactionalCallable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.dao.DataIntegrityViolationException;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class JooqSessaoRepositoryTest {
    @Test
    void devePropagarDataIntegrityViolationExceptionComCausa() {
        final var excecao = new DataIntegrityViolationException("", new SQLException("", "23514"));

        final var dsl = mock(DSLContext.class);
        when(dsl.transactionResult(ArgumentMatchers.<TransactionalCallable<Integer>>any())).thenThrow(excecao);

        final var repositorio = new JooqSessaoRepository(dsl);
        final var sessao = Sessao.criar(UUIDv7.criar());

        assertThatThrownBy(() -> repositorio.inserir(sessao)).isSameAs(excecao);
    }

    @Test
    void devePropagarDataIntegrityViolationExceptionSemCausa() {
        final var excecao = new DataIntegrityViolationException("");

        final var dsl = mock(DSLContext.class);
        when(dsl.transactionResult(ArgumentMatchers.<TransactionalCallable<Integer>>any())).thenThrow(excecao);

        final var repositorio = new JooqSessaoRepository(dsl);
        final var sessao = Sessao.criar(UUIDv7.criar());

        assertThatThrownBy(() -> repositorio.inserir(sessao)).isSameAs(excecao);
    }
}
