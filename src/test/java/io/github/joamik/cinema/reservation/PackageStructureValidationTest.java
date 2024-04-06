package io.github.joamik.cinema.reservation;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class PackageStructureValidationTest {

    private final ImportOption ignoreTests = location -> !location.contains("/test-classes");
    private final JavaClasses classes = new ClassFileImporter().withImportOption(ignoreTests).importPackages("io.github.joamik.cinema");

    private final String baseModule = "..base..";
    private final String reservationModule = "..reservation..";

    private final String domainPackage = "..domain..";
    private final String applicationPackage = "..application..";
    private final String apiPackage = "..api..";
    private final String infrastructurePackage = "..infrastructure..";
    private final String akkaPackage = "..akka..";

    @Test
    void shouldCheckDependenciesForDomainPackage() {
        // given
        ArchRule domainRules = noClasses()
                .that()
                .resideInAPackage(domainPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(applicationPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(apiPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(infrastructurePackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(akkaPackage);

        // when // then
        domainRules.check(classes);
    }

    @Test
    public void shouldCheckDependenciesForApplicationPackage() {
        // given
        ArchRule applicationRules = noClasses()
                .that()
                .resideInAPackage(applicationPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(apiPackage)
                .orShould()
                .accessClassesThat()
                .resideInAPackage(infrastructurePackage);

        // when // then
        applicationRules.check(classes);
    }

    @Test
    void shouldCheckDependenciesForApiPackage() {
        // given
        ArchRule apiRules = noClasses()
                .that()
                .resideInAPackage(apiPackage)
                .should()
                .accessClassesThat()
                .resideInAPackage(infrastructurePackage);

        // when // then
        apiRules.check(classes);
    }

    @Test
    void shouldCheckDependenciesForBasePackage() {
        // given
        ArchRule baseModuleRules = noClasses()
                .that()
                .resideInAPackage(baseModule)
                .should()
                .accessClassesThat()
                .resideInAPackage(reservationModule);

        // when // then
        baseModuleRules.check(classes);
    }
}
