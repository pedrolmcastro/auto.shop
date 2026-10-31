package shop.auto.api.modules.identity.application.ports.repositories;

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.Optional;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.modules.identity.domain.valueobjects.RefreshTokenHash;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;

public interface SessaoRepository {
    public static sealed interface ErroInsercao {
        public static record IdDuplicado(UUIDv7 id) implements ErroInsercao {}

        public static record UsuarioInexistente(UUIDv7 usuario) implements ErroInsercao {}

        public static record RefreshTokenDuplicado(RefreshTokenHash hash) implements ErroInsercao {}
    }

    public static sealed interface ResultadoRotacao {
        public static record Sucesso(Sessao sessao) implements ResultadoRotacao {}

        public static enum Erro implements ResultadoRotacao {
            SESSAO_INEXISTENTE,
            SESSAO_INATIVA,
            REFRESH_TOKEN_DIVERGENTE,
            REFRESH_TOKEN_DUPLICADO,
        }
    }

    @CheckReturnValue
    public abstract Optional<ErroInsercao> inserir(Sessao sessao);

    /** Busca a sessão pelo hash atual ou por um hash consumido anteriormente. */
    @CheckReturnValue
    public abstract Optional<Sessao> buscar(RefreshTokenHash hash);

    /**
     * Substitui o hash atual e registra seu consumo na mesma transação, sem
     * estender a sessão.
     */
    @CheckReturnValue
    public abstract ResultadoRotacao rotacionarRefreshToken(
        UUIDv7 sessao,
        RefreshTokenHash atual,
        RefreshTokenHash novo
    );
}
