package com.darach.openlibrarybooks.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Tests to validate dependency injection patterns with Hilt.
 */
class DependencyInjectionTest {

    @Test
    fun `ViewModels should be annotated with @HiltViewModel`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                it.hasAnnotationOf("HiltViewModel") ||
                    it.hasAnnotationWithName("HiltViewModel")
            }
    }

    @Test
    fun `Repository implementations should have @Inject constructor`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue {
                it.primaryConstructor?.hasAnnotationOf("Inject") == true ||
                    it.primaryConstructor?.hasAnnotationWithName("Inject") == true
            }
    }

    @Test
    fun `UseCases should have @Inject constructor`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                it.primaryConstructor?.hasAnnotationOf("Inject") == true ||
                    it.primaryConstructor?.hasAnnotationWithName("Inject") == true
            }
    }

    @Test
    fun `Hilt modules should be annotated with @Module and @InstallIn`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("Module")
            .assertTrue {
                (it.hasAnnotationOf("Module") || it.hasAnnotationWithName("Module")) &&
                    (it.hasAnnotationOf("InstallIn") || it.hasAnnotationWithName("InstallIn"))
            }
    }

    @Test
    fun `Classes with @Provides should be in a @Module`() {
        Konsist
            .scopeFromProject()
            .functions()
            .filter { function ->
                function.hasAnnotationOf("Provides") ||
                    function.hasAnnotationWithName("Provides")
            }
            .assertTrue { function ->
                val parentClass = function.containingDeclaration
                parentClass.hasAnnotationOf("Module") ||
                    parentClass.hasAnnotationWithName("Module")
            }
    }

    @Test
    fun `Classes with @Binds should be in a @Module and be abstract`() {
        Konsist
            .scopeFromProject()
            .functions()
            .filter { function ->
                function.hasAnnotationOf("Binds") ||
                    function.hasAnnotationWithName("Binds")
            }
            .assertTrue { function ->
                val parentClass = function.containingDeclaration
                (
                    parentClass.hasAnnotationOf("Module") ||
                        parentClass.hasAnnotationWithName("Module")
                    ) &&
                    (
                        parentClass.hasModifier("abstract") ||
                            function.hasModifier("abstract")
                        )
            }
    }

    @Test
    fun `Activities should be annotated with @AndroidEntryPoint if using Hilt`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filter { klass ->
                klass.hasParentClassWithName("ComponentActivity") ||
                    klass.hasParentClassWithName("AppCompatActivity") ||
                    klass.hasParentClassWithName("FragmentActivity")
            }
            .filter { klass ->
                // Check if class has any Hilt-injected fields
                klass.properties().any { property ->
                    property.hasAnnotationOf("Inject") ||
                        property.hasAnnotationWithName("Inject")
                }
            }
            .assertTrue {
                it.hasAnnotationOf("AndroidEntryPoint") ||
                    it.hasAnnotationWithName("AndroidEntryPoint")
            }
    }
}
