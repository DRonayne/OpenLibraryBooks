package com.darach.openlibrarybooks.core.database.di

import android.content.Context
import androidx.room.Room
import com.darach.openlibrarybooks.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing database dependencies.
 * Configures Room with KSP and RxJava3 support.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides Room database instance configured with RxJava3 support.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    )
        .fallbackToDestructiveMigration() // For development; will be removed in production
        .build()

    // DAOs will be provided here in future implementation tickets
}
