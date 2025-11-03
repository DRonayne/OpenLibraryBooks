package com.darach.openlibrarybooks.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Tests to validate naming conventions across the codebase.
 */
class NamingConventionTest {

    @Test
    fun `classes extending ViewModel should have ViewModel suffix`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.hasNameEndingWith("ViewModel") }
    }

    @Test
    fun `ViewModels should reside in presentation package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                it.resideInPackage("..presentation..") ||
                    it.resideInPackage("..feature..")
            }
    }

    @Test
    fun `Repository interfaces should have Repository suffix`() {
        Konsist
            .scopeFromProject()
            .interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { it.hasNameEndingWith("Repository") }
    }

    @Test
    fun `Repository implementations should have RepositoryImpl suffix`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue {
                it.hasNameEndingWith("RepositoryImpl") &&
                    it.hasParentInterfaceWithName { name -> name.endsWith("Repository") }
            }
    }

    @Test
    fun `UseCases should have UseCase suffix`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue { it.hasNameEndingWith("UseCase") }
    }

    @Test
    fun `UseCases should reside in domain package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                it.resideInPackage("..domain..")
            }
    }

    @Test
    fun `Repositories should reside in domain or data package`() {
        Konsist
            .scopeFromProject()
            .interfaces()
            .withNameEndingWith("Repository")
            .assertTrue {
                it.resideInPackage("..domain..") ||
                    it.resideInPackage("..data..")
            }
    }

    @Test
    fun `Mapper classes should have Mapper suffix`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("Mapper")
            .assertTrue { it.hasNameEndingWith("Mapper") }
    }

    @Test
    fun `Entity classes should reside in data or database package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("Entity")
            .assertTrue {
                it.resideInPackage("..data..") ||
                    it.resideInPackage("..database..")
            }
    }

    @Test
    fun `DTO classes should reside in network package`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("Dto")
            .assertTrue {
                it.resideInPackage("..network..") ||
                    it.resideInPackage("..remote..")
            }
    }
}
