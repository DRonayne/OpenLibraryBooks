package com.darach.openlibrarybooks.core.network.di

import com.darach.openlibrarybooks.core.network.api.OpenLibraryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing network dependencies.
 * Configures Retrofit with RxJava3, Gson, and OkHttp with logging.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org"
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 60L

    /**
     * Provides configured OkHttpClient with:
     * - Logging interceptor (debug only)
     * - 30s connect timeout
     * - 60s read timeout
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                // In debug builds, log full request/response bodies
                // In release builds, disable logging
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    /**
     * Provides configured Retrofit instance with:
     * - RxJava3 CallAdapter for reactive streams
     * - Gson converter for JSON parsing
     * - Base URL for Open Library API
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(OPEN_LIBRARY_BASE_URL)
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Provides Open Library API service interface.
     */
    @Provides
    @Singleton
    fun provideOpenLibraryApi(retrofit: Retrofit): OpenLibraryApi = retrofit.create(OpenLibraryApi::class.java)
}
