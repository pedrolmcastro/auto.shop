package shop.auto.api.modules.identity.domain.valueobjects;

import com.google.errorprone.annotations.CheckReturnValue;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record RefreshTokenHash(String valor) {
    public RefreshTokenHash {
        if (!valor.matches("[0-9a-f]{64}")) {
            throw new InvarianteException.FormatoInvalido();
        }
    }

    @CheckReturnValue
    public static RefreshTokenHash hash(final RefreshToken token) {
        try {
            final var digest = MessageDigest.getInstance("SHA-256");
            final var hash = digest.digest(token.valor().getBytes(StandardCharsets.UTF_8));
            return new RefreshTokenHash(HexFormat.of().formatHex(hash));
        } catch (final NoSuchAlgorithmException excecao) {
            throw new InvarianteException.AlgoritmoIndisponivel(excecao);
        }
    }

    @CheckReturnValue
    @Override
    public String toString() {
        return "RefreshTokenHash[valor=*]";
    }

    public abstract static sealed class InvarianteException extends IllegalArgumentException {
        public static final class FormatoInvalido extends InvarianteException {
            private FormatoInvalido() {
                super("Hash do refresh token deve conter 64 caracteres hexadecimais minúsculos");
            }
        }

        public static final class AlgoritmoIndisponivel extends InvarianteException {
            private AlgoritmoIndisponivel(final NoSuchAlgorithmException causa) {
                super("SHA-256 deve estar disponível no ambiente Java", causa);
            }
        }

        private InvarianteException(final String mensagem) {
            super(mensagem);
        }

        private InvarianteException(final String mensagem, final Throwable causa) {
            super(mensagem, causa);
        }
    }
}
