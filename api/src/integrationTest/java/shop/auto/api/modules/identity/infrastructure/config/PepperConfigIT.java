package shop.auto.api.modules.identity.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import shop.auto.api.modules.identity.application.ports.PepperProvider;
import shop.auto.api.modules.identity.domain.resources.Pepper;
import static org.assertj.core.api.Assertions.assertThat;

public final class PepperConfigIT {
    @Test
    void deveDisponibilizarProvider(@TempDir final Path pasta) throws IOException {
        final var bytes = new byte[Pepper.COMPRIMENTO];
        Arrays.fill(bytes, (byte) 1);

        final var caminho = Files.write(pasta.resolve("pepper"), bytes);

        new ApplicationContextRunner()
            .withUserConfiguration(PepperConfig.class)
            .withPropertyValues("identity.pepper.file=" + caminho)
            .run(contexto -> {
                assertThat(contexto).hasSingleBean(PepperProvider.class);

                try (final var pepper = contexto.getBean(PepperProvider.class).obter()) {
                    pepper.usar(segredo -> assertThat(HexFormat.of().parseHex(segredo)).containsExactly(bytes));
                }
            });
    }
}
