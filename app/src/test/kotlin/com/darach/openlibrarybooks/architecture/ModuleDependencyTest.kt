package com.darach.openlibrarybooks.architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.Test

/**
 * Tests to validate module dependency rules.
 * Core modules should not depend on feature modules.
 * Feature modules can depend on core modules.
 */
class ModuleDependencyTest {

    @Test
    fun `core modules should not depend on feature modules`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { it.path.contains("core") }
            .forEach { file ->
                file.imports
                    .filter { it.name.startsWith("com.darach.openlibrarybooks.feature") }
                    .also { imports ->
                        if (imports.isNotEmpty()) {
                            throw AssertionError(
                                "Core module ${file.path} should not depend on feature modules. " +
                                    "Found imports: ${imports.map { it.name }}",
                            )
                        }
                    }
            }
    }

    @Test
    fun `feature modules should not depend on other feature modules`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { it.path.contains("feature") }
            .forEach { file ->
                val currentFeature = file.path.substringAfter("feature/").substringBefore("/")
                file.imports
                    .filter { import ->
                        import.name.startsWith("com.darach.openlibrarybooks.feature") &&
                            !import.name.contains("feature.$currentFeature")
                    }
                    .also { imports ->
                        if (imports.isNotEmpty()) {
                            throw AssertionError(
                                "Feature module ${file.path} should not depend on other feature modules. " +
                                    "Found imports: ${imports.map { it.name }}",
                            )
                        }
                    }
            }
    }
}
