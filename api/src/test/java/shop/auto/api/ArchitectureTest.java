package shop.auto.api;

import com.google.common.annotations.VisibleForTesting;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.context.annotation.Configuration;
import shop.auto.api.shared.utils.VisibleForRepository;
import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "shop.auto.api", importOptions = ImportOption.DoNotIncludeTests.class)
public final class ArchitectureTest {
    @ArchTest
    static final ArchRule ADAPTERS_DEVEM_SER_INDEPENDENTES = slices()
        .matching("..infrastructure.adapters.(*)..")
        .should()
        .notDependOnEachOther()
        .because("Adapters da camada de infrastructure devem ser independentes");

    @ArchTest
    static final ArchRule CAMADAS_DEVEM_RESPEITAR_CLEAN_ARCHITECTURE = layeredArchitecture()
        .consideringOnlyDependenciesInAnyPackage("shop.auto.api..")
        .layer("Application")
        .definedBy("..application..")
        .layer("Domain")
        .definedBy("..domain..")
        .layer("Infrastructure")
        .definedBy("..infrastructure..")
        .layer("Utils")
        .definedBy("..utils..")
        .whereLayer("Application")
        .mayOnlyAccessLayers("Domain", "Utils")
        .whereLayer("Domain")
        .mayOnlyAccessLayers("Utils")
        .whereLayer("Infrastructure")
        .mayOnlyAccessLayers("Application", "Domain", "Utils")
        .whereLayer("Utils")
        .mayNotAccessAnyLayer();

    @ArchTest
    static final ArchRule CODIGO_GENERATED_DEVE_SER_INDEPENDENTE = noClasses()
        .that()
        .resideInAPackage("..infrastructure.generated..")
        .should()
        .dependOnClassesThat(
            resideInAPackage("shop.auto.api..").and(resideOutsideOfPackage("..infrastructure.generated.."))
        )
        .because("Código gerado automaticamente deve ser independente do restante da aplicação");

    @ArchTest
    static final ArchRule CONFIGS_DEVEM_TER_ANOTACAO = classes()
        .that()
        .resideInAPackage("..infrastructure.config..")
        .should()
        .beAnnotatedWith(Configuration.class)
        .because("Configs da camada de infrastructure devem ser anotadas com @Configuration");

    @ArchTest
    static final ArchRule JOOQ_DEVE_SER_USADO_SOMENTE_NA_CAMADA_DE_INFRASTRUCTURE = noClasses()
        .that()
        .resideOutsideOfPackage("..infrastructure..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.jooq..")
        .because("jOOQ deve ser usado apenas na camada de infrastructure");

    @ArchTest
    static final ArchRule MIGRATIONS_DEVEM_IMPLEMENTAR_JAVA_MIGRATION = classes()
        .that()
        .resideInAPackage("..infrastructure.migration..")
        .should()
        .beAssignableTo(JavaMigration.class)
        .because("Migrations definidas em Java na camada de infrastructure devem implementar o contrato do Flyway");

    @ArchTest
    static final ArchRule MODULOS_DEVEM_SER_ACICLICOS = slices()
        .matching("..modules.(*)..")
        .should()
        .beFreeOfCycles()
        .because("Módulos devem manter uma direção de dependências sem ciclos");

    @ArchTest
    static final ArchRule PORTS_DEVEM_SER_INTERFACES = classes()
        .that()
        .resideInAPackage("..application.ports..")
        .and()
        .areTopLevelClasses()
        .should()
        .beInterfaces()
        .because("Ports da camada de application devem ser apenas contratos");

    @ArchTest
    static final ArchRule SHARED_DEVE_SER_INDEPENDENTE = noClasses()
        .that()
        .resideInAPackage("..shared..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..modules..")
        .because("O módulo shared deve ser independente de outros módulos");

    @ArchTest
    static final ArchRule SPRING_DEVE_SER_USADO_SOMENTE_NA_CAMADA_DE_INFRASTRUCTURE = noClasses()
        .that()
        .resideOutsideOfPackage("..infrastructure..")
        .and()
        .doNotHaveFullyQualifiedName(Application.class.getName())
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.springframework..")
        .because("Spring deve ser usado apenas na camada de infrastructure e na Application");

    @ArchTest
    static final ArchRule SUBCAMADAS_DE_DOMAIN_DEVEM_RESPEITAR_CLEAN_ARCHITECTURE = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Entities")
        .definedBy("..domain.entities..")
        .layer("Value objects")
        .definedBy("..domain.valueobjects..")
        .layer("Resources")
        .definedBy("..domain.resources..")
        .whereLayer("Entities")
        .mayOnlyAccessLayers("Resources", "Value objects")
        .whereLayer("Value objects")
        .mayOnlyAccessLayers("Resources")
        .whereLayer("Resources")
        .mayNotAccessAnyLayer();

    @ArchTest
    static final ArchRule SUBCAMADAS_DE_INFRASTRUCTURE_DEVEM_RESPEITAR_CLEAN_ARCHITECTURE = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Adapters")
        .definedBy("..infrastructure.adapters..")
        .layer("Config")
        .definedBy("..infrastructure.config..")
        .layer("Generated")
        .definedBy("..infrastructure.generated..")
        .layer("Migration")
        .definedBy("..infrastructure.migration..")
        .whereLayer("Adapters")
        .mayOnlyAccessLayers("Generated")
        .whereLayer("Config")
        .mayOnlyAccessLayers("Adapters")
        .whereLayer("Migration")
        .mayOnlyAccessLayers("Generated")
        .whereLayer("Generated")
        .mayNotAccessAnyLayer();

    @ArchTest
    static final ArchRule VISIBLE_FOR_REPOSITORY_DEVE_SER_USADO_APENAS_POR_REPOSITORIOS = noClasses()
        .should()
        .accessTargetWhere(
            describe(
                "acessar membro @VisibleForRepository fora dos adapters de persistence",
                (JavaAccess<?> chamada) -> {
                    return chamada.getTarget().isAnnotatedWith(VisibleForRepository.class)
                        && !chamada.getOriginOwner().equals(chamada.getTargetOwner())
                        && !resideInAPackage("..infrastructure.adapters.persistence..").test(chamada.getOriginOwner());
                }
            )
        )
        .because("Métodos @VisibleForRepository devem ser reservados à repository persistence adapters");

    @ArchTest
    static final ArchRule VISIBLE_FOR_TESTING_DEVE_SER_USADO_APENAS_PELA_CLASSE = noClasses()
        .should()
        .accessTargetWhere(
            describe(
                "acessar membro marcado @VisibleForTesting de outra classe",
                (JavaAccess<?> chamada) -> chamada.getTarget().isAnnotatedWith(VisibleForTesting.class)
                    && !chamada.getOriginOwner().equals(chamada.getTargetOwner())
            )
        )
        .because("Métodos @VisibleForTesting devem ser reservados à testes");

    private ArchitectureTest() {}
}
