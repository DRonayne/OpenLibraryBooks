package com.darach.openlibrarybooks.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Tests to validate package structure conventions.
 */
class PackageStructureTest {

    @Test
    fun `all files should have correct package declaration`() {
        Konsist
            .scopeFromProject()
            .files
            .assertTrue {
                val expectedPackage = it.path
                    .substringAfter("kotlin/")
                    .substringBeforeLast("/")
                    .replace("/", ".")

                it.packageDeclaration?.name == expectedPackage
            }
    }

    @Test
    fun `network layer classes should reside in network package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filter { klass ->
                klass.name.endsWith("Api") ||
                    klass.name.endsWith("Service") ||
                    klass.name.endsWith("Client")
            }
            .assertTrue {
                it.resideInPackage("..network..")
            }
    }

    @Test
    fun `database layer classes should reside in database package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filter { klass ->
                klass.name.endsWith("Dao") ||
                    klass.name.endsWith("Database") ||
                    klass.name.endsWith("Entity")
            }
            .assertTrue {
                it.resideInPackage("..database..")
            }
    }

    @Test
    fun `UI components should reside in ui or presentation package`() {
        Konsist
            .scopeFromProject()
            .functions()
            .filter { function ->
                function.hasAnnotationOf("Composable")
            }
            .assertTrue {
                it.resideInPackage("..ui..") ||
                    it.resideInPackage("..presentation..") ||
                    it.resideInPackage("..feature..")
            }
    }

    @Test
    fun `domain models should not have framework annotations`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filter { it.resideInPackage("..domain..model..") }
            .assertTrue { klass ->
                val frameworkAnnotations = klass.annotations.filter { annotation ->
                    annotation.name.startsWith("androidx.") ||
                        annotation.name.startsWith("android.") ||
                        annotation.name == "Serializable" ||
                        annotation.name == "Parcelize"
                }
                frameworkAnnotations.isEmpty()
            }
    }

    @Test
    fun `test classes should reside in test package matching their source`() {
        Konsist
            .scopeFromTest()
            .classes()
            .filter { it.name.endsWith("Test") }
            .assertTrue { testClass ->
                val testPackage = testClass.packageDeclaration?.name ?: ""
                val sourceClassName = testClass.name.removeSuffix("Test")

                // Find corresponding source class
                val sourceClasses = Konsist
                    .scopeFromProduction()
                    .classes()
                    .filter { it.name == sourceClassName }

                if (sourceClasses.isEmpty()) {
                    // Architecture tests or other tests that don't have corresponding source
                    true
                } else {
                    // Verify test is in the same package or a test-specific sub-package
                    sourceClasses.any { sourceClass ->
                        val sourcePackage = sourceClass.packageDeclaration?.name ?: ""
                        testPackage == sourcePackage ||
                            testPackage.startsWith(sourcePackage)
                    }
                }
            }
    }
}
