package com.darach.openlibrarybooks.feature.widget.di

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing Coil ImageLoader for image loading in widgets.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    /**
     * Provides a singleton ImageLoader instance for loading book cover images.
     *
     * @param context Application context
     * @return Configured ImageLoader instance
     */
    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader = ImageLoader.Builder(context)
        .components {
            add(OkHttpNetworkFetcherFactory())
        }
        .build()
}
