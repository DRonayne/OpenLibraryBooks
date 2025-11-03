package com.darach.openlibrarybooks.core.data.di

import com.darach.openlibrarybooks.core.data.repository.BooksRepositoryImpl
import com.darach.openlibrarybooks.core.data.repository.FavouritesRepositoryImpl
import com.darach.openlibrarybooks.core.domain.repository.BooksRepository
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing repository implementations.
 *
 * Binds repository interfaces to their concrete implementations
 * for dependency injection throughout the app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /**
     * Provide BooksRepository implementation.
     *
     * @param impl The concrete implementation
     * @return The repository interface
     */
    @Binds
    @Singleton
    abstract fun bindBooksRepository(impl: BooksRepositoryImpl): BooksRepository

    /**
     * Provide FavouritesRepository implementation.
     *
     * @param impl The concrete implementation
     * @return The repository interface
     */
    @Binds
    @Singleton
    abstract fun bindFavouritesRepository(impl: FavouritesRepositoryImpl): FavouritesRepository
}
