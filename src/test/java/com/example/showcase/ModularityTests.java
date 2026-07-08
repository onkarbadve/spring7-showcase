package com.example.showcase;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulith treats each direct sub-package of this package
 * ({@code catalog}, {@code client}, {@code config}, {@code order},
 * {@code runtime}, {@code spotlight}) as an application module, inferred
 * from the package structure itself - no separate module descriptor needed.
 * {@link #verifiesModularStructure()} fails the build if a module reaches
 * into another module's internals or if the modules form a dependency cycle.
 */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(ShowcaseApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
