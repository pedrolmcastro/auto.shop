package shop.auto.api.modulos.identidade.infraestrutura.configuracao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import shop.auto.api.modulos.identidade.aplicacao.portas.PepperProvider;
import static org.assertj.core.api.Assertions.assertThat;

public final class PepperConfigIT {
    @Test
    void deveDisponibilizarProvedorComArquivoConfigurado(@TempDir final Path pasta) throws IOException {
        final var bytes = new byte[32];
        bytes[0] = 42;
        final var caminho = Files.write(pasta.resolve("pepper"), bytes);

        new ApplicationContextRunner()
            .withUserConfiguration(PepperConfig.class)
            .withPropertyValues("identidade.pepper.arquivo=" + caminho)
            .run(contexto -> {
                assertThat(contexto).hasSingleBean(PepperProvider.class);

                try (final var pepper = contexto.getBean(PepperProvider.class).obter()) {
                    pepper.usar(segredo -> assertThat(HexFormat.of().parseHex(segredo)).containsExactly(bytes));
                }
            });
    }
}
