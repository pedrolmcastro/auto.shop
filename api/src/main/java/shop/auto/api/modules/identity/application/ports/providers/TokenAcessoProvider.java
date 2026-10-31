package shop.auto.api.modules.identity.application.ports.providers;

import com.google.errorprone.annotations.CheckReturnValue;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.modules.identity.domain.valueobjects.TokenAcesso;

public interface TokenAcessoProvider {
    public static sealed interface ResultadoEmissao {
        public static record Sucesso(TokenAcesso token) implements ResultadoEmissao {}

        public static enum Erro implements ResultadoEmissao {
            SESSAO_INATIVA,
            TEMPO_INSUFICIENTE,
        }
    }

    @CheckReturnValue
    public abstract ResultadoEmissao emitir(Sessao sessao);
}
