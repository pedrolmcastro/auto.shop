package shop.auto.api.modules.identity.application.ports.repositories;

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.Optional;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;

public interface SessaoRepository {
    public static sealed interface ErroInsercao {
        public static record IdDuplicado(UUIDv7 id) implements ErroInsercao {}

        public static record UsuarioInexistente(UUIDv7 usuario) implements ErroInsercao {}
    }

    @CheckReturnValue
    public abstract Optional<ErroInsercao> inserir(Sessao sessao);
}
