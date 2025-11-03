package com.darach.openlibrarybooks.core.common.performance

import android.util.Log
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for Firebase Performance Monitoring throughout the app.
 * Provides custom traces for measuring performance of key operations.
 */
@Singleton
@Suppress("TooGenericExceptionCaught") // Catches all exceptions for graceful fallback
class FirebasePerformanceHelper @Inject constructor(val performance: FirebasePerformance) {

    companion object {
        private const val TAG = "FirebasePerformance"

        // Trace names
        const val TRACE_APP_STARTUP = "app_startup"
        const val TRACE_BOOK_SYNC = "book_sync"
        const val TRACE_DATABASE_QUERY = "database_query"
        const val TRACE_IMAGE_LOAD = "image_load"
        const val TRACE_SCREEN_RENDER = "screen_render"
        const val TRACE_WIDGET_UPDATE = "widget_update"
        const val TRACE_CACHE_IMAGES = "cache_images"

        // Metric names
        const val METRIC_BOOKS_COUNT = "books_count"
        const val METRIC_IMAGES_COUNT = "images_count"
        const val METRIC_DB_ROWS = "db_rows"
        const val METRIC_CACHE_SIZE = "cache_size"
        const val METRIC_NETWORK_BYTES = "network_bytes"

        // Attribute names
        const val ATTR_SCREEN_NAME = "screen_name"
        const val ATTR_SYNC_TYPE = "sync_type"
        const val ATTR_QUERY_TYPE = "query_type"
        const val ATTR_SUCCESS = "success"
        const val ATTR_ERROR_TYPE = "error_type"
    }

    /**
     * Starts a custom trace for performance monitoring.
     * Remember to call stopTrace() when the operation completes.
     */
    fun startTrace(traceName: String): Trace {
        val trace = performance.newTrace(traceName)
        trace.start()
        Log.d(TAG, "Trace started: $traceName")
        return trace
    }

    /**
     * Stops a trace and uploads the results.
     */
    fun stopTrace(trace: Trace) {
        try {
            trace.stop()
            Log.d(TAG, "Trace stopped: ${trace.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop trace", e)
        }
    }

    /**
     * Executes a block of code and measures its performance with a trace.
     * Automatically starts and stops the trace.
     */
    inline fun <T> measureTrace(traceName: String, block: (Trace) -> T): T {
        val trace = performance.newTrace(traceName)
        trace.start()
        return try {
            block(trace)
        } finally {
            trace.stop()
        }
    }

    /**
     * Measures app startup time.
     */
    fun traceAppStartup(block: () -> Unit) {
        measureTrace(TRACE_APP_STARTUP) {
            block()
        }
    }

    /**
     * Measures book sync operation with metrics.
     */
    fun traceBookSync(syncType: String, block: (Trace) -> Unit) {
        measureTrace(TRACE_BOOK_SYNC) { trace ->
            trace.putAttribute(ATTR_SYNC_TYPE, syncType)
            try {
                block(trace)
                trace.putAttribute(ATTR_SUCCESS, "true")
            } catch (e: Exception) {
                trace.putAttribute(ATTR_SUCCESS, "false")
                trace.putAttribute(ATTR_ERROR_TYPE, e.javaClass.simpleName)
                throw e
            }
        }
    }

    /**
     * Measures database query performance.
     */
    fun traceDatabaseQuery(queryType: String, block: (Trace) -> Unit) {
        measureTrace(TRACE_DATABASE_QUERY) { trace ->
            trace.putAttribute(ATTR_QUERY_TYPE, queryType)
            block(trace)
        }
    }

    /**
     * Measures image loading performance.
     */
    fun traceImageLoad(block: () -> Unit) {
        measureTrace(TRACE_IMAGE_LOAD) {
            block()
        }
    }

    /**
     * Measures screen rendering time.
     */
    fun traceScreenRender(screenName: String, block: () -> Unit) {
        measureTrace(TRACE_SCREEN_RENDER) { trace ->
            trace.putAttribute(ATTR_SCREEN_NAME, screenName)
            block()
        }
    }

    /**
     * Measures widget update performance.
     */
    fun traceWidgetUpdate(block: (Trace) -> Unit) {
        measureTrace(TRACE_WIDGET_UPDATE) { trace ->
            block(trace)
        }
    }

    /**
     * Measures cache image operations.
     */
    fun traceCacheImages(block: (Trace) -> Unit) {
        measureTrace(TRACE_CACHE_IMAGES) { trace ->
            block(trace)
        }
    }

    /**
     * Enables or disables performance monitoring data collection.
     */
    fun setPerformanceCollectionEnabled(enabled: Boolean) {
        try {
            performance.isPerformanceCollectionEnabled = enabled
            Log.d(TAG, "Performance collection enabled: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set performance collection enabled state", e)
        }
    }

    /**
     * Adds a metric to a trace.
     */
    fun Trace.addMetric(metricName: String, value: Long) {
        try {
            incrementMetric(metricName, value)
            Log.d(TAG, "Metric added to ${this.name}: $metricName = $value")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add metric to trace", e)
        }
    }

    /**
     * Adds an attribute to a trace for filtering in Firebase Console.
     */
    fun Trace.addAttribute(attributeName: String, attributeValue: String) {
        try {
            putAttribute(attributeName, attributeValue)
            Log.d(TAG, "Attribute added to ${this.name}: $attributeName = $attributeValue")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add attribute to trace", e)
        }
    }
}
