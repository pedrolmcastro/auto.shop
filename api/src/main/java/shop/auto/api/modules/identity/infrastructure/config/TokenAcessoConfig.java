package shop.auto.api.modules.identity.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import shop.auto.api.modules.identity.application.ports.providers.TokenAcessoProvider;
import shop.auto.api.modules.identity.infrastructure.adapters.security.NimbusTokenAcessoProvider;

@Configuration(proxyBeanMethods = false)
public class TokenAcessoConfig {
    @Bean
    public TokenAcessoProvider tokenAcessoProvider(
        @Value(value = "${identity.jwt.private-key-file}") final String caminhoChavePrivada,
        @Value(value = "${identity.jwt.public-key-file}") final String caminhoChavePublica
    ) throws IOException {
        try (
            final var arquivoChavePrivada = Files.newInputStream(Path.of(caminhoChavePrivada));
            final var arquivoChavePublica = Files.newInputStream(Path.of(caminhoChavePublica))
        ) {
            final var chavePrivada = Objects.requireNonNull(RsaKeyConverters.pkcs8().convert(arquivoChavePrivada));
            final var chavePublica = Objects.requireNonNull(RsaKeyConverters.x509().convert(arquivoChavePublica));
            final var encoder = NimbusJwtEncoder.withKeyPair(chavePublica, chavePrivada).build();
            return new NimbusTokenAcessoProvider(encoder);
        }
    }
}
