package com.portafolio_manager.backend;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(
        packages = "com.portafolio_manager.backend"
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule hexagonalArchitecture =
            layeredArchitecture()
                    .consideringOnlyDependenciesInAnyPackage(
                            "com.portafolio_manager.backend.."
                    )
                    .withOptionalLayers(true)

                    .layer("Domain")
                    .definedBy("..domain..")

                    .layer("Application")
                    .definedBy("..application..")

                    .layer("Infrastructure")
                    .definedBy("..infrastructure..")

                    .whereLayer("Domain")
                    .mayNotAccessAnyLayer()

                    .whereLayer("Application")
                    .mayOnlyAccessLayers(
                            "Domain"
                    )

                    .whereLayer("Infrastructure")
                    .mayOnlyAccessLayers(
                            "Application",
                            "Domain"
                    );
}