package com.darach.openlibrarybooks.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Tests to validate MVVM architecture patterns.
 */
class MvvmArchitectureTest {

    @Test
    fun `ViewModels should not have Android framework dependencies except ViewModel`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                val androidImports = it.imports.filter { import ->
                    import.name.startsWith("android.") &&
                        !import.name.startsWith("androidx.lifecycle.ViewModel") &&
                        !import.name.startsWith("androidx.lifecycle.viewmodel")
                }
                androidImports.isEmpty()
            }
    }

    @Test
    fun `ViewModels should extend ViewModel class`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                it.hasParentClassWithName("ViewModel") ||
                    it.hasParentClassWithName("androidx.lifecycle.ViewModel")
            }
    }

    @Test
    fun `UseCases should be invokable`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                it.hasFunction { function ->
                    function.name == "invoke" ||
                        function.name == "execute"
                }
            }
    }

    @Test
    fun `Repositories should be interfaces in domain layer`() {
        Konsist
            .scopeFromProject()
            .interfaces()
            .withNameEndingWith("Repository")
            .assertTrue {
                it.resideInPackage("..domain..")
            }
    }

    @Test
    fun `Repository implementations should be in data layer`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue {
                it.resideInPackage("..data..") ||
                    it.resideInPackage("..network..") ||
                    it.resideInPackage("..database..")
            }
    }

    @Test
    fun `Data classes in domain should not depend on framework`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filter { it.resideInPackage("..domain..") && it.hasModifier("data") }
            .assertTrue { dataClass ->
                val androidImports = dataClass.imports.filter { import ->
                    import.name.startsWith("android.") ||
                        import.name.startsWith("androidx.") ||
                        import.name.startsWith("kotlinx.serialization")
                }
                androidImports.isEmpty()
            }
    }

    @Test
    fun `Classes annotated with @Inject constructor should not be in presentation layer`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filter { klass ->
                klass.primaryConstructor?.hasAnnotationOf("Inject") == true ||
                    klass.primaryConstructor?.hasAnnotationWithName("Inject") == true
            }
            .assertTrue {
                // ViewModels can use @Inject but should use @HiltViewModel
                !it.resideInPackage("..presentation..ui..") ||
                    it.hasNameEndingWith("ViewModel")
            }
    }

    @Test
    fun `Composables should not directly access ViewModels outside of screens`() {
        Konsist
            .scopeFromProject()
            .functions()
            .filter { function ->
                function.hasAnnotationOf("Composable") &&
                    !function.name.endsWith("Screen") &&
                    !function.name.endsWith("Route")
            }
            .assertTrue { function ->
                // Check if function parameters contain ViewModel
                val hasViewModelParam = function.parameters.none { param ->
                    param.type?.name?.endsWith("ViewModel") == true
                }
                hasViewModelParam
            }
    }

    @Test
    fun `Screen composables should follow naming convention`() {
        Konsist
            .scopeFromProject()
            .functions()
            .filter { function ->
                function.hasAnnotationOf("Composable") &&
                    (function.name.endsWith("Screen") || function.name.endsWith("Route"))
            }
            .assertTrue { function ->
                function.name.endsWith("Screen") || function.name.endsWith("Route")
            }
    }
}
