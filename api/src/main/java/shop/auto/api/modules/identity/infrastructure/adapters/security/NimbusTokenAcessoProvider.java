package shop.auto.api.modules.identity.infrastructure.adapters.security;

import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.time.InstantSource;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import shop.auto.api.modules.identity.application.ports.providers.TokenAcessoProvider;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.modules.identity.domain.valueobjects.TokenAcesso;

public final class NimbusTokenAcessoProvider implements TokenAcessoProvider {
    public static final Duration DURACAO = Duration.ofMinutes(5);

    private final JwtEncoder encoder;
    private final InstantSource relogio;

    public NimbusTokenAcessoProvider(final JwtEncoder encoder) {
        this(encoder, InstantSource.system());
    }

    @VisibleForTesting
    public NimbusTokenAcessoProvider(final JwtEncoder encoder, final InstantSource relogio) {
        this.encoder = encoder;
        this.relogio = relogio;
    }

    @Override
    public ResultadoEmissao emitir(final Sessao sessao) {
        if (!(sessao.estado() instanceof Sessao.Estado.Ativa(var sessaoExpiraEm))) {
            return ResultadoEmissao.Erro.SESSAO_INATIVA;
        }

        final var emitidoEm = this.relogio.instant().truncatedTo(ChronoUnit.SECONDS);

        final var tokenExpiraEm = emitidoEm.plus(DURACAO);
        final var sessaoExpiraEmTruncado = sessaoExpiraEm.truncatedTo(ChronoUnit.SECONDS);

        final var expiraEm = sessaoExpiraEmTruncado.isBefore(tokenExpiraEm)
            ? sessaoExpiraEmTruncado
            : tokenExpiraEm;

        if (!expiraEm.isAfter(emitidoEm)) {
            return ResultadoEmissao.Erro.TEMPO_INSUFICIENTE;
        }

        return new ResultadoEmissao.Sucesso(
            new TokenAcesso(
                this.encoder
                    .encode(
                        JwtEncoderParameters
                            .from(
                                JwsHeader.with(TokenAcesso.ALGORITMO).type(TokenAcesso.tipo()).build(),
                                JwtClaimsSet
                                    .builder()
                                    .audience(List.of(TokenAcesso.AUDIENCIA))
                                    .expiresAt(expiraEm)
                                    .issuedAt(emitidoEm)
                                    .issuer(TokenAcesso.EMISSOR)
                                    .subject(sessao.usuario().toString())
                                    .build()
                            )
                    )
            )
        );
    }
}
