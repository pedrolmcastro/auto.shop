package shop.auto.api.modules.identity.infrastructure.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.auto.api.modules.identity.application.ports.providers.PepperProvider;
import shop.auto.api.modules.identity.infrastructure.adapters.filesystem.FilesystemPepperProvider;

@Configuration(proxyBeanMethods = false)
public class PepperConfig {
    @Bean
    public PepperProvider pepperProvider(@Value(value = "${identity.pepper.file}") final String caminho) {
        return new FilesystemPepperProvider(Path.of(caminho));
    }
}
