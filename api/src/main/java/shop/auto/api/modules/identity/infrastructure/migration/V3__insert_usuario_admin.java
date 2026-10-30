package shop.auto.api.modules.identity.infrastructure.migration;

import com.password4j.SecureString;
import java.sql.SQLException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;
import shop.auto.api.modules.identity.application.ports.providers.PepperProvider;
import shop.auto.api.modules.identity.domain.valueobjects.NomeUsuario;
import shop.auto.api.modules.identity.domain.valueobjects.SenhaProtegida;
import shop.auto.api.shared.domain.valueobjects.UUIDv7;

@Component
public final class V3__insert_usuario_admin extends BaseJavaMigration {
    private final PepperProvider pepperProvider;

    public V3__insert_usuario_admin(final PepperProvider pepperProvider) {
        this.pepperProvider = pepperProvider;
    }

    @Override
    public void migrate(final Context contexto) throws SQLException {
        final var senha = new SecureString(new char[] { 'a', 'd', 'm', 'i', 'n', });

        try (
            final var pepper = this.pepperProvider.obter();
            final var comando = contexto
                .getConnection()
                .prepareStatement("INSERT INTO identity.usuario (id, nome, senha) VALUES (?, ?, ?)")
        ) {
            comando.setObject(1, UUIDv7.criar().valor());
            comando.setString(2, new NomeUsuario("admin").valor());
            comando.setString(3, SenhaProtegida.proteger(senha, pepper).valor());
            comando.executeUpdate();
        } finally {
            senha.clear();
        }
    }
}
