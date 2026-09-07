package com.tsw.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "com.tsw",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class BackendArchitectureTest {

    @ArchTest
    static final ArchRule layer_dependencies_should_be_respected = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
            .because("requests should pass from controllers through services to repositories");

    @ArchTest
    static final ArchRule top_level_packages_should_be_free_of_cycles = slices()
            .matching("com.tsw.(*)..")
            .should().beFreeOfCycles()
            .because("cyclic package dependencies make layer boundaries unstable");

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories_directly = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .because("controllers should access persistence through services");

    @ArchTest
    static final ArchRule controllers_should_not_expose_persistence_models = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..model..")
            .because("the REST contract should use dedicated request and response DTOs");

    @ArchTest
    static final ArchRule services_should_not_depend_on_spring_web = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.web..")
            .because("transport-specific types belong to the web adapter");

    @ArchTest
    static final ArchRule models_should_not_depend_on_outer_layers = noClasses()
            .that().resideInAPackage("..model..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..controller..",
                    "..service..",
                    "..repository..",
                    "..dto..",
                    "..config.."
            )
            .because("the model should not depend on outer application layers");

    @ArchTest
    static final ArchRule dto_should_not_depend_on_implementation_layers = noClasses()
            .that().resideInAPackage("..dto..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..controller..",
                    "..service..",
                    "..repository..",
                    "..model..",
                    "..config.."
            )
            .because("API contracts should remain independent of their implementation");

    @ArchTest
    static final ArchRule controllers_should_have_consistent_names = classes()
            .that().resideInAPackage("..controller..")
            .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule controllers_should_be_annotated = classes()
            .that().resideInAPackage("..controller..")
            .should().beAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule rest_controllers_should_reside_in_controller_package = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule services_should_have_consistent_names = classes()
            .that().resideInAPackage("..service..")
            .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule services_should_be_annotated = classes()
            .that().resideInAPackage("..service..")
            .should().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule annotated_services_should_reside_in_service_package = classes()
            .that().areAnnotatedWith(Service.class)
            .should().resideInAPackage("..service..");

    @ArchTest
    static final ArchRule repositories_should_have_consistent_names = classes()
            .that().resideInAPackage("..repository..")
            .should().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule repositories_should_be_annotated = classes()
            .that().resideInAPackage("..repository..")
            .should().beAnnotatedWith(Repository.class);

    @ArchTest
    static final ArchRule annotated_repositories_should_reside_in_repository_package = classes()
            .that().areAnnotatedWith(Repository.class)
            .should().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule repositories_should_be_interfaces = classes()
            .that().resideInAPackage("..repository..")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule entities_should_reside_in_model_package = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..model..");

    @ArchTest
    static final ArchRule field_injection_should_not_be_used = NO_CLASSES_SHOULD_USE_FIELD_INJECTION
            .because("constructor injection makes dependencies explicit and testable");
}
