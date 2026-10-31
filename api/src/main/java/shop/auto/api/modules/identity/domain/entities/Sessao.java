package shop.auto.api.modules.identity.domain.entities;

import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import org.jspecify.annotations.Nullable;
import shop.auto.api.modules.identity.domain.valueobjects.RefreshTokenHash;
import shop.auto.api.shared.domain.entities.Entity;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import shop.auto.api.shared.utils.VisibleForRepository;

public final class Sessao extends Entity {
    public static final Duration DURACAO = Duration.ofHours(8);

    private final UUIDv7 usuario;
    private final RefreshTokenHash refreshTokenHash;
    private final Instant criadaEm;
    private final Instant expiraEm;
    private @Nullable Instant revogadaEm;
    private final InstantSource relogio;

    @VisibleForRepository
    public Sessao(
        final UUIDv7 id,
        final UUIDv7 usuario,
        final RefreshTokenHash refreshTokenHash,
        final Instant criadaEm,
        final Instant expiraEm,
        final @Nullable Instant revogadaEm
    ) {
        this(id, usuario, refreshTokenHash, criadaEm, expiraEm, revogadaEm, InstantSource.system());
    }

    @VisibleForTesting
    public Sessao(
        final UUIDv7 id,
        final UUIDv7 usuario,
        final RefreshTokenHash refreshTokenHash,
        final Instant criadaEm,
        final Instant expiraEm,
        final @Nullable Instant revogadaEm,
        final InstantSource relogio
    ) {
        final var agora = relogio.instant();

        if (criadaEm.isAfter(agora)) {
            throw new InvarianteException.CriacaoInvalida(agora, criadaEm);
        }

        if (!expiraEm.isAfter(criadaEm)) {
            throw new InvarianteException.ExpiracaoInvalida(criadaEm, expiraEm);
        }

        if (revogadaEm != null) {
            if (revogadaEm.isBefore(criadaEm)) {
                throw new InvarianteException.Revogacao.AntesDaCriacao(criadaEm, revogadaEm);
            }

            if (!revogadaEm.isBefore(expiraEm)) {
                throw new InvarianteException.Revogacao.DepoisDaExpiracao(expiraEm, revogadaEm);
            }
        }

        super(id);

        this.usuario = usuario;
        this.refreshTokenHash = refreshTokenHash;
        this.criadaEm = criadaEm;
        this.expiraEm = expiraEm;
        this.revogadaEm = revogadaEm;
        this.relogio = relogio;
    }

    public static Sessao criar(final UUIDv7 usuario, final RefreshTokenHash refreshTokenHash) {
        return criar(usuario, refreshTokenHash, InstantSource.system());
    }

    @VisibleForTesting
    public static Sessao criar(
        final UUIDv7 usuario,
        final RefreshTokenHash refreshTokenHash,
        final InstantSource source
    ) {
        final var criadaEm = source.instant();
        return new Sessao(UUIDv7.criar(), usuario, refreshTokenHash, criadaEm, criadaEm.plus(DURACAO), null, source);
    }

    @VisibleForRepository
    public Instant criadaEm() {
        return this.criadaEm;
    }

    public Estado estado() {
        return estado(relogio.instant());
    }

    private Estado estado(final Instant agora) {
        if (this.revogadaEm != null) {
            return new Estado.Revogada(this.revogadaEm);
        }

        if (!agora.isBefore(this.expiraEm)) {
            return new Estado.Expirada(this.expiraEm);
        }

        return new Estado.Ativa(this.expiraEm);
    }

    @VisibleForRepository
    public Instant expiraEm() {
        return this.expiraEm;
    }

    public RefreshTokenHash refreshTokenHash() {
        return this.refreshTokenHash;
    }

    public void revogar() {
        final var agora = this.relogio.instant();

        switch (estado(agora)) {
            case Estado.Ativa _ -> this.revogadaEm = agora;
            case Estado.Expirada _ -> { /* Nada a ser feito */ }
            case Estado.Revogada _ -> { /* Nada a ser feito */ }
        }
    }

    public UUIDv7 usuario() {
        return this.usuario;
    }

    public static sealed interface Estado {
        public record Ativa(Instant expiraEm) implements Estado {}

        public record Expirada(Instant expiradaEm) implements Estado {}

        public record Revogada(Instant revogadaEm) implements Estado {}
    }

    public abstract static sealed class InvarianteException extends IllegalArgumentException {
        public static final class CriacaoInvalida extends InvarianteException {
            private CriacaoInvalida(final Instant agora, final Instant criadaEm) {
                super(String.format("Criação deve ser anterior ao instante atual %s mas é %s", agora, criadaEm));
            }
        }

        public static final class ExpiracaoInvalida extends InvarianteException {
            private ExpiracaoInvalida(final Instant criadaEm, final Instant expiraEm) {
                super(String.format("Expiração deve ser posterior à criação %s mas é %s", criadaEm, expiraEm));
            }
        }

        public abstract static sealed class Revogacao extends InvarianteException {
            public static final class AntesDaCriacao extends Revogacao {
                private AntesDaCriacao(final Instant criadaEm, final Instant revogadaEm) {
                    super(String.format("Revogação deve ser posterior à criação %s mas é %s", criadaEm, revogadaEm));
                }
            }

            public static final class DepoisDaExpiracao extends Revogacao {
                private DepoisDaExpiracao(final Instant expiraEm, final Instant revogadaEm) {
                    super(String.format("Revogação deve ser anterior à expiração %s mas é %s", expiraEm, revogadaEm));
                }
            }

            private Revogacao(final String mensagem) {
                super(mensagem);
            }
        }

        private InvarianteException(final String mensagem) {
            super(mensagem);
        }
    }
}
