package shop.auto.api.modulos.identidade.infraestrutura.configuracao;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.auto.api.modulos.identidade.aplicacao.portas.PepperProvider;
import shop.auto.api.modulos.identidade.infraestrutura.recursos.ArquivoPepperProvider;

@Configuration(proxyBeanMethods = false)
public class PepperConfig {
    @Bean
    public PepperProvider pepperProvider(@Value(value = "${identidade.pepper.arquivo}") final String caminho) {
        return new ArquivoPepperProvider(Path.of(caminho));
    }
}
