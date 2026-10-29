package shop.auto.api.modulos.identidade.dominio.objetosdevalor;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.SecureString;
import com.password4j.types.Argon2;
import java.util.Base64;
import java.util.Locale;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.InvalidaException.ComprimentoInsuficienteException;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.InvalidaException.FormatoInvalidoException;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.InvalidaException.IteracoesInsuficientesException;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.InvalidaException.MemoriaInsuficienteException;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.InvalidaException.ParalelismoInvalidoException;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.InvalidaException.SaltInvalidoException;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.InvalidaException.VarianteInvalidaException;
import shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.InvalidaException.VersaoInvalidaException;
import shop.auto.api.modulos.identidade.dominio.recursos.Pepper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.ARGON2;
import static shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.COMPRIMENTO;
import static shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.COMPRIMENTO_SALT;
import static shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.ITERACOES;
import static shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.MEMORIA;
import static shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.PARALELISMO;
import static shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.VARIANTE;
import static shop.auto.api.modulos.identidade.dominio.objetosdevalor.SenhaProtegida.VERSAO;

public final class SenhaProtegidaTest {
    private static final String SALT = Base64.getEncoder().withoutPadding().encodeToString(new byte[COMPRIMENTO_SALT]);

    private static final String HASH = String
        .format(
            "$argon2%s$v=%d$m=%d,t=%d,p=%d$%s$%s",
            VARIANTE.name().toLowerCase(Locale.ROOT),
            VERSAO,
            MEMORIA,
            ITERACOES,
            PARALELISMO,
            SALT,
            Base64.getEncoder().withoutPadding().encodeToString(new byte[COMPRIMENTO])
        );

    private static final Pepper PEPPER = new Pepper(new byte[Pepper.COMPRIMENTO]);

    @Nested
    final class Equals {
        @Test
        void deveSerFalsoQuandoNaoForSenhaProtegida() {
            assertThat(SenhaProtegida.reconstruir(HASH)).isNotEqualTo(new Object());
        }

        @Test
        void deveSerFalsoQuandoValoresSaoDiferentes() {
            final var senha = SenhaProtegida.reconstruir(HASH);
            final var outra = SenhaProtegida.reconstruir(HASH.replace("t=" + ITERACOES, "t=" + (ITERACOES + 1)));

            assertThat(senha).isNotEqualTo(outra);
        }

        @Test
        void deveSerVerdadeiroQuandoValoresSaoIguais() {
            assertThat(SenhaProtegida.reconstruir(HASH)).isEqualTo(SenhaProtegida.reconstruir(HASH));
        }
    }

    @Nested
    final class HashCode {
        @Test
        void deveSerBaseadoNoValor() {
            assertThat(SenhaProtegida.reconstruir(HASH)).hasSameHashCodeAs(HASH);
        }
    }

    @Nested
    final class Proteger {
        @Test
        void deveGerarSaltAleatorio() {
            final var senha = new SecureString("senha".toCharArray());

            final var primeira = SenhaProtegida.proteger(senha, PEPPER);
            final var segunda = SenhaProtegida.proteger(senha, PEPPER);

            assertThat(primeira).isNotEqualTo(segunda);
            assertThat(primeira.salt()).isNotEqualTo(segunda.salt());
        }

        @Test
        void deveProtegerSenhaComArgon2id() {
            final var senha = SenhaProtegida.proteger(new SecureString("senha".toCharArray()), PEPPER);
            final var funcao = Argon2Function.getInstanceFromHash(senha.valor());

            assertThat(funcao).isEqualTo(ARGON2);
            assertThat(funcao.getOutputLength()).isEqualTo(COMPRIMENTO);
            assertThat(funcao.getIterations()).isEqualTo(ITERACOES);
            assertThat(funcao.getMemory()).isEqualTo(MEMORIA);
            assertThat(funcao.getParallelism()).isEqualTo(PARALELISMO);
            assertThat(funcao.getVariant()).isEqualTo(VARIANTE);
            assertThat(funcao.getVersion()).isEqualTo(VERSAO);
            assertThat(Base64.getDecoder().decode(senha.salt())).hasSize(COMPRIMENTO_SALT);
        }
    }

    @Nested
    final class Reconstruir {
        @ParameterizedTest
        @ValueSource(ints = { COMPRIMENTO_SALT, COMPRIMENTO_SALT + 1, COMPRIMENTO_SALT * 2 })
        void deveAceitarComprimentoSaltSuficiente(final int comprimento) {
            final var salt = Base64.getEncoder().withoutPadding().encodeToString(new byte[comprimento]);
            final var valor = HASH.replace("$" + SALT + "$", "$" + salt + "$");

            assertThat(SenhaProtegida.reconstruir(valor).valor()).isEqualTo(valor);
            assertThat(SenhaProtegida.reconstruir(valor).salt()).isEqualTo(salt);
        }

        @ParameterizedTest
        @ValueSource(ints = { COMPRIMENTO, COMPRIMENTO + 1, COMPRIMENTO * 2 })
        void deveAceitarComprimentoSuficiente(final int comprimento) {
            final var valor = HASH.substring(0, HASH.lastIndexOf('$') + 1) + Base64
                .getEncoder()
                .withoutPadding()
                .encodeToString(new byte[comprimento]);

            assertThat(SenhaProtegida.reconstruir(valor).valor()).isEqualTo(valor);
        }

        @ParameterizedTest
        @ValueSource(ints = { ITERACOES, ITERACOES + 1 })
        void deveAceitarIteracoesSuficientes(final int iteracoes) {
            final var valor = HASH.replace("t=" + ITERACOES, "t=" + iteracoes);

            assertThat(SenhaProtegida.reconstruir(valor).valor()).isEqualTo(valor);
        }

        @ParameterizedTest
        @ValueSource(ints = { MEMORIA, MEMORIA + 1, MEMORIA * 2 })
        void deveAceitarMemoriaSuficiente(final int memoria) {
            final var valor = HASH.replace("m=" + MEMORIA, "m=" + memoria);

            assertThat(SenhaProtegida.reconstruir(valor).valor()).isEqualTo(valor);
        }

        @Test
        void deveReconstruirSenhaProtegida() {
            final var senha = SenhaProtegida.proteger(new SecureString("senha".toCharArray()), PEPPER);
            assertThat(SenhaProtegida.reconstruir(senha.valor())).isEqualTo(senha);
        }

        @ParameterizedTest
        @ValueSource(ints = { 1, COMPRIMENTO / 2, COMPRIMENTO - 1 })
        void deveRejeitarComprimentoInsuficiente(final int comprimento) {
            final var valor = HASH.substring(0, HASH.lastIndexOf('$') + 1) + Base64
                .getEncoder()
                .withoutPadding()
                .encodeToString(new byte[comprimento]);

            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(ComprimentoInsuficienteException.class)
                .hasMessage("Hash da senha deve ter no mínimo %d bytes mas tem %d", COMPRIMENTO, comprimento);
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, COMPRIMENTO_SALT - 1 })
        void deveRejeitarComprimentoSaltInsuficiente(final int comprimento) {
            final var salt = Base64.getEncoder().withoutPadding().encodeToString(new byte[comprimento]);
            final var valor = HASH.replace("$" + SALT + "$", "$" + salt + "$");

            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(SaltInvalidoException.ComprimentoInvalidoException.class)
                .hasMessage("Salt da senha deve ter no mínimo %d bytes mas tem %d", COMPRIMENTO_SALT, comprimento);
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "invalido", "$2a$12$invalido", "$argon2id$v=19$m=x,t=2,p=1$c2Fsd$aGFzaA" })
        void deveRejeitarFormatoInvalido(final String valor) {
            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(FormatoInvalidoException.class)
                .hasMessage("Senha deve ser protegida com hash Argon2id")
                .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void deveRejeitarFormatoSaltInvalido() {
            final var salt = Base64.getEncoder().encodeToString(new byte[COMPRIMENTO_SALT]) + "!";
            final var valor = HASH.replace("$" + SALT + "$", "$" + salt + "$");

            assertThat(Argon2Function.getInstanceFromHash(valor)).isEqualTo(ARGON2);
            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(SaltInvalidoException.FormatoInvalidoException.class)
                .hasMessage("Salt da senha deve estar codificado em Base64")
                .hasCauseExactlyInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, ITERACOES - 1 })
        void deveRejeitarIteracoesInsuficientes(final int iteracoes) {
            final var valor = HASH.replace("t=" + ITERACOES, "t=" + iteracoes);

            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(IteracoesInsuficientesException.class)
                .hasMessage("Hash da senha deve usar no mínimo %d iterações mas usa %d", ITERACOES, iteracoes);
        }

        @ParameterizedTest
        @ValueSource(ints = { 8, MEMORIA - 1 })
        void deveRejeitarMemoriaInsuficiente(final int memoria) {
            final var valor = HASH.replace("m=" + MEMORIA, "m=" + memoria);

            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(MemoriaInsuficienteException.class)
                .hasMessage("Hash da senha deve usar no mínimo %d KiB de memória mas usa %d", MEMORIA, memoria);
        }

        @ParameterizedTest
        @ValueSource(ints = { PARALELISMO + 1, PARALELISMO + 3 })
        void deveRejeitarParalelismoInvalido(final int paralelismo) {
            final var valor = HASH.replace("p=" + PARALELISMO, "p=" + paralelismo);

            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(ParalelismoInvalidoException.class)
                .hasMessage("Hash da senha deve paralelismo %d mas usa %d", PARALELISMO, paralelismo);
        }

        @ParameterizedTest
        @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = "ID", value = Argon2.class)
        void deveRejeitarVarianteInvalida(final Argon2 variante) {
            final var valor = HASH
                .replace(
                    "argon2" + VARIANTE.name().toLowerCase(Locale.ROOT),
                    "argon2" + variante.name().toLowerCase(Locale.ROOT)
                );

            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(VarianteInvalidaException.class)
                .hasMessage(
                    "Hash da senha deve usar a variante Argon2id mas usa Argon2%s",
                    variante.name().toLowerCase(Locale.ROOT)
                );
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 1, VERSAO - 1, VERSAO + 1 })
        void deveRejeitarVersaoInvalida(final int versao) {
            final var valor = HASH.replace("v=" + VERSAO, "v=" + versao);

            assertThatThrownBy(() -> SenhaProtegida.reconstruir(valor))
                .isExactlyInstanceOf(VersaoInvalidaException.class)
                .hasMessage("Hash da senha deve usar a versão %d do Argon2 mas usa %d", VERSAO, versao);
        }
    }

    @Nested
    final class Salt {
        @Test
        void deveExporSalt() {
            assertThat(SenhaProtegida.reconstruir(HASH).salt()).isEqualTo(SALT);
        }
    }

    @Nested
    final class ToString {
        @Test
        void deveOcultarValor() {
            assertThat(SenhaProtegida.reconstruir(HASH)).hasToString("SenhaProtegida[valor=*]");
        }
    }

    @Nested
    final class Valor {
        @Test
        void deveExporValorReconstruido() {
            assertThat(SenhaProtegida.reconstruir(HASH).valor()).isEqualTo(HASH);
        }
    }

    @Nested
    final class Verificar {
        @Test
        void deveAceitarSenhaCorreta() {
            final var senha = "senha".toCharArray();

            assertThat(
                SenhaProtegida
                    .proteger(new SecureString(senha), PEPPER)
                    .verificar(new SecureString(senha.clone()), PEPPER)
            ).isTrue();
        }

        @Test
        void deveAceitarSenhaReconstruidaComParametrosDiferentes() {
            final var senha = new SecureString("senha".toCharArray());

            assertThat(
                SenhaProtegida
                    .reconstruir(
                        PEPPER
                            .usar(
                                p -> Password
                                    .hash(senha)
                                    .addRandomSalt(COMPRIMENTO_SALT + 1)
                                    .addPepper(p)
                                    .with(
                                        Argon2Function
                                            .getInstance(
                                                MEMORIA + 1,
                                                ITERACOES + 1,
                                                PARALELISMO,
                                                COMPRIMENTO + 1,
                                                VARIANTE,
                                                VERSAO
                                            )
                                    )
                                    .getResult()
                            )
                    )
                    .verificar(senha, PEPPER)
            ).isTrue();
        }

        @Test
        void deveRejeitarPepperDiferente() {
            final var senha = new SecureString("senha".toCharArray());

            final var pepper = new byte[Pepper.COMPRIMENTO];
            pepper[0] = 1;

            assertThat(SenhaProtegida.proteger(senha, PEPPER).verificar(senha, new Pepper(pepper))).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "outra", "Senha" })
        void deveRejeitarSenhaIncorreta(final String senha) {
            assertThat(
                SenhaProtegida
                    .proteger(new SecureString("senha".toCharArray()), PEPPER)
                    .verificar(new SecureString(senha.toCharArray()), PEPPER)
            ).isFalse();
        }
    }
}
