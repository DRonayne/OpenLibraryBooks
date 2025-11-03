package com.darach.openlibrarybooks.core.network.interceptor

import android.util.Log
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.HttpMetric
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp interceptor that automatically tracks network request performance using Firebase Performance Monitoring.
 *
 * This interceptor creates a trace for each HTTP request and records:
 * - Request method and URL
 * - Response time
 * - Response code
 * - Response payload size
 * - Any errors that occur
 *
 * All network requests are automatically tracked without any additional code in repositories.
 */
@Suppress(
    "TooGenericExceptionCaught",
    "NestedBlockDepth",
) // Intentional for graceful fallback and required for interceptor logic
class FirebasePerformanceInterceptor : Interceptor {

    companion object {
        private const val TAG = "FirebasePerfInterceptor"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method

        // Create a new HttpMetric for this network call
        val metric: HttpMetric? = try {
            FirebasePerformance.getInstance()
                .newHttpMetric(url, method)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Firebase Performance metric", e)
            null
        }

        // Start the metric
        metric?.start()

        val startTime = System.currentTimeMillis()
        var response: Response? = null

        try {
            // Proceed with the actual network request
            response = chain.proceed(request)

            // Record successful response details
            metric?.let {
                it.setHttpResponseCode(response.code)
                response.body?.contentLength()?.let { size ->
                    if (size != -1L) {
                        it.setResponsePayloadSize(size)
                    }
                }
                response.request.body?.contentLength()?.let { size ->
                    if (size != -1L) {
                        it.setRequestPayloadSize(size)
                    }
                }
            }

            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Request to $url completed in ${duration}ms with code ${response.code}")

            return response
        } catch (e: IOException) {
            // Record the error
            metric?.let {
                // Mark as failed by setting a 0 response code
                it.setHttpResponseCode(0)
            }

            Log.e(TAG, "Request to $url failed after ${System.currentTimeMillis() - startTime}ms", e)
            throw e
        } finally {
            // Stop and record the metric
            metric?.stop()
        }
    }
}
