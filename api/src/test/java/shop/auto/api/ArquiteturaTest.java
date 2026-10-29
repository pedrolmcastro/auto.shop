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
    public static final ArchTests COMPARTILHADO = ArchTests.in(Compartilhado.class);

    @ArchTest
    public static final ArchTests OBJETOS_DE_VALOR = ArchTests.in(ObjetosDeValor.class);

    @ArchTest
    public static final ArchTests RECURSOS = ArchTests.in(Recursos.class);

    @ArchTest
    public static final ArchTests UTILITARIOS = ArchTests.in(Utilitarios.class);

    private ArquiteturaTest() {}

    public static final class Compartilhado {
        @ArchTest
        static final ArchRule NAO_DEVE_DEPENDER_DE_MODULOS = naoDependerDe(
            "..compartilhado..",
            "..modulos..",
            "O módulo compartilhado deve ser independentes de outros módulos"
        );
    }

    public static final class ObjetosDeValor {
        @ArchTest
        static final ArchRule NAO_DEVEM_DEPENDER_DE_ENTIDADES = naoDependerDe(
            "..dominio.objetosdevalor..",
            "..dominio.entidades..",
            "Objetos de valor devem ser independentes de entidades"
        );
    }

    public static final class Recursos {
        @ArchTest
        static final ArchRule NAO_DEVEM_DEPENDER_DE_ENTIDADES = naoDependerDe(
            "..dominio.recursos..",
            "..dominio.entidades..",
            "Recursos devem ser independentes de entidades"
        );

        @ArchTest
        static final ArchRule NAO_DEVEM_DEPENDER_DE_OBJETOS_DE_VALOR = naoDependerDe(
            "..dominio.recursos..",
            "..dominio.objetosdevalor..",
            "Recursos devem ser independentes de objetos de valor"
        );
    }

    public static final class Utilitarios {
        @ArchTest
        static final ArchRule NAO_DEVEM_DEPENDER_DO_DOMINIO = naoDependerDe(
            "..utilitarios..",
            "..dominio..",
            "Funções utilitárias devem ser independentes do domínio"
        );
    }

    private static ArchRule naoDependerDe(final String origem, final String destino, final String motivo) {
        return noClasses()
            .that()
            .resideInAPackage(origem)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(destino)
            .because(motivo);
    }
}
