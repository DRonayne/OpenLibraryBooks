package com.darach.openlibrarybooks

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.darach.openlibrarybooks.core.common.analytics.FirebaseAnalyticsHelper
import com.darach.openlibrarybooks.core.common.crashlytics.FirebaseCrashlyticsHelper
import com.darach.openlibrarybooks.core.common.performance.FirebasePerformanceHelper
import com.darach.openlibrarybooks.feature.widget.WidgetUpdater
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for Open Library Books app.
 * Configured with Hilt for dependency injection, WorkManager integration, and Firebase services.
 */
@HiltAndroidApp
class OpenLibraryApplication :
    Application(),
    Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var analyticsHelper: FirebaseAnalyticsHelper

    @Inject
    lateinit var crashlyticsHelper: FirebaseCrashlyticsHelper

    @Inject
    lateinit var performanceHelper: FirebasePerformanceHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Measure app startup time with Firebase Performance
        performanceHelper.traceAppStartup {
            initializeFirebase()
            initializeApp()
        }

        // Log app open event
        analyticsHelper.logAppOpen()
        Log.i(TAG, "Open Library Books app started successfully")
    }

    /**
     * Initialises Firebase services with error handling.
     */
    @Suppress("TooGenericExceptionCaught") // Intentional catch-all for graceful app startup
    private fun initializeFirebase() {
        try {
            // Firebase is automatically initialised, but we verify it here
            FirebaseApp.getInstance()
            Log.d(TAG, "Firebase initialised successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialise Firebase", e)
            crashlyticsHelper.recordException(e, "Firebase initialisation failed")
        }
    }

    /**
     * Initialises app components.
     */
    @Suppress("TooGenericExceptionCaught") // Intentional catch-all for graceful app startup
    private fun initializeApp() {
        try {
            // Update widget when app launches to refresh favourite books
            WidgetUpdater.updateFavouritesWidget(this)
            Log.d(TAG, "Widget updater initialised")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widget during app launch", e)
            crashlyticsHelper.recordException(e, "Widget update failed during app launch")
        }
    }

    companion object {
        private const val TAG = "OpenLibraryApp"
    }
}
