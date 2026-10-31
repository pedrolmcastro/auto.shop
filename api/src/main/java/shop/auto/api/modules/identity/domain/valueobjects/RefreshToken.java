package shop.auto.api.modules.identity.domain.valueobjects;

import com.google.errorprone.annotations.CheckReturnValue;
import java.security.SecureRandom;
import java.util.Base64;

public record RefreshToken(String valor) {
    public static final int COMPRIMENTO_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    public RefreshToken {
        if (!valor.matches("[A-Za-z0-9_-]{43}")) {
            throw new InvarianteException.FormatoInvalido();
        }

        final var bytes = Base64.getUrlDecoder().decode(valor);
        final var canonico = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        if (!canonico.equals(valor)) {
            throw new InvarianteException.CodificacaoNaoCanonica();
        }
    }

    @CheckReturnValue
    public static RefreshToken criar() {
        final var bytes = new byte[COMPRIMENTO_BYTES];
        RANDOM.nextBytes(bytes);
        return new RefreshToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    @CheckReturnValue
    @Override
    public String toString() {
        return "RefreshToken[valor=*]";
    }

    public abstract static sealed class InvarianteException extends IllegalArgumentException {
        public static final class FormatoInvalido extends InvarianteException {
            private FormatoInvalido() {
                super("Refresh token deve conter 43 caracteres Base64 URL-safe sem padding");
            }
        }

        public static final class CodificacaoNaoCanonica extends InvarianteException {
            private CodificacaoNaoCanonica() {
                super("Refresh token deve usar codificação Base64 URL-safe canônica");
            }
        }

        private InvarianteException(final String mensagem) {
            super(mensagem);
        }
    }
}
