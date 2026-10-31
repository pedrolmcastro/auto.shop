package shop.auto.api.modules.identity.infrastructure.config;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;
import shop.auto.api.modules.identity.application.ports.providers.TokenAcessoProvider;
import shop.auto.api.modules.identity.application.ports.providers.TokenAcessoProvider.ResultadoEmissao;
import shop.auto.api.modules.identity.domain.entities.Sessao;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class TokenAcessoConfigIT {
    @Test
    void deveDisponibilizarProvider() throws IOException {
        final var privada = new ClassPathResource("jwt-test-private.pem").getFile();
        final var publica = new ClassPathResource("jwt-test-public.pem").getFile();

        new ApplicationContextRunner()
            .withUserConfiguration(TokenAcessoConfig.class)
            .withPropertyValues(
                "identity.jwt.private-key-file=" + privada.getAbsolutePath(),
                "identity.jwt.public-key-file=" + publica.getAbsolutePath()
            )
            .run(contexto -> {
                assertThat(contexto).hasSingleBean(TokenAcessoProvider.class);

                final var jwt = SignedJWT
                    .parse(
                        assertInstanceOf(
                            ResultadoEmissao.Sucesso.class,
                            contexto.getBean(TokenAcessoProvider.class).emitir(Sessao.criar(UUIDv7.criar()))
                        ).token().valor()
                    );

                try (final var arquivo = Files.newInputStream(publica.toPath())) {
                    final var chavePublica = Objects.requireNonNull(RsaKeyConverters.x509().convert(arquivo));
                    assertThat(jwt.verify(new RSASSAVerifier(chavePublica))).isTrue();
                }
            });
    }

    @Test
    void deveRejeitarArquivosAusentes(@TempDir final Path pasta) {
        new ApplicationContextRunner()
            .withUserConfiguration(TokenAcessoConfig.class)
            .withPropertyValues(
                "identity.jwt.private-key-file=" + pasta.resolve("inexistente-private.pem"),
                "identity.jwt.public-key-file=" + pasta.resolve("inexistente-public.pem")
            )
            .run(contexto -> assertThat(contexto).hasFailed());
    }
}
