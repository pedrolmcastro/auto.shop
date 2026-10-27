package shop.auto.api;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "shop.auto.api", importOptions = ImportOption.DoNotIncludeTests.class)
public final class ArquiteturaTest {
    @ArchTest
    public static final ArchTests OBJETOS_DE_VALOR = ArchTests.in(ObjetosDeValor.class);

    private ArquiteturaTest() {}

    public static final class ObjetosDeValor {
        @ArchTest
        static void naoDevemDependerDeEntidades(final JavaClasses classes) {
            noClasses()
                .that()
                .resideInAPackage("..dominio.objetosdevalor..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..dominio.entidades..")
                .because("Objetos de valor devem ser independentes de entidades")
                .check(classes);
        }
    }
}
