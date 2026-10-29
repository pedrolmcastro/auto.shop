package shop.auto.api;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "shop.auto.api", importOptions = ImportOption.DoNotIncludeTests.class)
public final class ArquiteturaTest {
    @ArchTest
    public static final ArchTests CAMADAS = ArchTests.in(Camadas.class);

    @ArchTest
    public static final ArchTests MODULOS = ArchTests.in(Modulos.class);

    public static final class Camadas {
        @ArchTest
        public static final ArchTests APLICACAO = ArchTests.in(Aplicacao.class);

        @ArchTest
        public static final ArchTests DOMINIO = ArchTests.in(Dominio.class);

        @ArchTest
        public static final ArchTests UTILITARIOS = ArchTests.in(Utilitarios.class);

        public static final class Aplicacao {
            private static final String PACOTE = "..aplicacao..";

            @ArchTest
            static final ArchRule NAO_DEVE_DEPENDER_DA_INFRAESTRUTURA = naoDeveDependerDe(
                PACOTE,
                Infraestrutura.PACOTE,
                "A camada de aplicação deve ser independente da camada de infraestrutura"
            );
        }

        public static final class Dominio {
            @ArchTest
            public static final ArchTests OBJETOS_DE_VALOR = ArchTests.in(ObjetosDeValor.class);

            @ArchTest
            public static final ArchTests RECURSOS = ArchTests.in(Recursos.class);

            private static final String PREFIXO = "..dominio";
            private static final String PACOTE = PREFIXO + "..";

            @ArchTest
            static final ArchRule NAO_DEVE_DEPENDER_DA_APLICACAO = naoDeveDependerDe(
                PACOTE,
                Aplicacao.PACOTE,
                "A camada de domínio deve ser independentes da camada de aplicação"
            );

            @ArchTest
            static final ArchRule NAO_DEVE_DEPENDER_DA_INFRAESTRUTURA = naoDeveDependerDe(
                PACOTE,
                Infraestrutura.PACOTE,
                "A camada de domínio deve ser independentes da camada de infraestrutura"
            );

            public static final class Entidades {
                private static final String PACOTE = Dominio.PREFIXO + ".entidades..";
            }

            public static final class ObjetosDeValor {
                private static final String PACOTE = Dominio.PREFIXO + ".objetosdevalor..";

                @ArchTest
                static final ArchRule NAO_DEVEM_DEPENDER_DE_ENTIDADES = naoDeveDependerDe(
                    PACOTE,
                    Entidades.PACOTE,
                    "Objetos de valor devem ser independentes de entidades"
                );
            }

            public static final class Recursos {
                private static final String PACOTE = Dominio.PREFIXO + ".recursos..";

                @ArchTest
                static final ArchRule NAO_DEVEM_DEPENDER_DE_ENTIDADES = naoDeveDependerDe(
                    PACOTE,
                    Entidades.PACOTE,
                    "Recursos devem ser independentes de entidades"
                );

                @ArchTest
                static final ArchRule NAO_DEVEM_DEPENDER_DE_OBJETOS_DE_VALOR = naoDeveDependerDe(
                    PACOTE,
                    ObjetosDeValor.PACOTE,
                    "Recursos devem ser independentes de objetos de valor"
                );
            }
        }

        public static final class Infraestrutura {
            private static final String PACOTE = "..infraestrutura..";
        }

        public static final class Utilitarios {
            private static final String PACOTE = "..utilitarios..";

            @ArchTest
            static final ArchRule NAO_DEVEM_DEPENDER_DA_APLICACAO = naoDeveDependerDe(
                PACOTE,
                Aplicacao.PACOTE,
                "Utilitários devem ser independentes da camada de aplicação"
            );

            @ArchTest
            static final ArchRule NAO_DEVEM_DEPENDER_DO_DOMINIO = naoDeveDependerDe(
                PACOTE,
                Dominio.PACOTE,
                "Utilitários devem ser independentes da camada de domínio"
            );

            @ArchTest
            static final ArchRule NAO_DEVEM_DEPENDER_DA_INFRAESTRUTURA = naoDeveDependerDe(
                PACOTE,
                Infraestrutura.PACOTE,
                "Utilitários devem ser independentes da camada de infraestrutura"
            );
        }
    }

    public static final class Modulos {
        @ArchTest
        public static final ArchTests COMPARTILHADO = ArchTests.in(Compartilhado.class);

        public static final class Compartilhado {
            @ArchTest
            static final ArchRule NAO_DEVE_DEPENDER_DE_MODULOS = naoDeveDependerDe(
                "..compartilhado..",
                "..modulos..",
                "O módulo compartilhado deve ser independentes de outros módulos"
            );
        }
    }

    private ArquiteturaTest() {}

    private static ArchRule naoDeveDependerDe(final String origem, final String destino, final String motivo) {
        return noClasses()
            .that()
            .resideInAPackage(origem)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(destino)
            .because(motivo);
    }
}
