package com.darach.openlibrarybooks.feature.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache for widget book cover images.
 * Downloads and stores book covers in internal storage for use in Glance widgets,
 * since Glance doesn't support Coil's AsyncImage directly.
 */
@Singleton
class WidgetImageCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
) {

    companion object {
        private const val TAG = "WidgetImageCache"
        private const val CACHE_DIR_NAME = "widget_covers"
        private const val IMAGE_QUALITY = 85 // JPEG quality 0-100
        private const val MAX_WIDTH = 200 // Max width for cached images
        private const val MAX_HEIGHT = 300 // Max height for cached images
    }

    private val cacheDir: File by lazy {
        File(context.filesDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
                Log.d(TAG, "Created widget cache directory: $absolutePath")
            }
        }
    }

    /**
     * Downloads and caches a book cover image.
     *
     * @param bookId Unique book identifier
     * @param coverUrl URL of the cover image
     * @return True if successfully cached, false otherwise
     */
    suspend fun cacheImage(bookId: String, coverUrl: String): Boolean = try {
        Log.d(TAG, "Caching image for book: $bookId from $coverUrl")

        val request = ImageRequest.Builder(context)
            .data(coverUrl)
            .size(MAX_WIDTH, MAX_HEIGHT)
            .build()

        val result = imageLoader.execute(request)
        if (result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            saveBitmapToCache(bookId, bitmap)
            Log.d(TAG, "Successfully cached image for book: $bookId")
            true
        } else {
            Log.e(TAG, "Failed to load image for book: $bookId")
            false
        }
    } catch (e: IOException) {
        Log.e(TAG, "IO error caching image for book: $bookId", e)
        false
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "Invalid image data for book: $bookId", e)
        false
    }

    /**
     * Retrieves a cached book cover bitmap.
     *
     * @param bookId Unique book identifier
     * @return Cached bitmap or null if not found
     */
    fun getCachedImage(bookId: String): Bitmap? {
        val file = getCacheFile(bookId)
        return if (file.exists()) {
            try {
                BitmapFactory.decodeFile(file.absolutePath)?.also {
                    Log.d(TAG, "Retrieved cached image for book: $bookId")
                }
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Out of memory reading cached image for book: $bookId", e)
                null
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid bitmap file for book: $bookId", e)
                null
            }
        } else {
            null
        }
    }

    /**
     * Checks if an image is cached for a book.
     *
     * @param bookId Unique book identifier
     * @return True if cached image exists
     */
    fun isCached(bookId: String): Boolean = getCacheFile(bookId).exists()

    /**
     * Removes a cached image.
     *
     * @param bookId Unique book identifier
     * @return True if successfully deleted or didn't exist
     */
    fun removeImage(bookId: String): Boolean {
        val file = getCacheFile(bookId)
        return if (file.exists()) {
            file.delete().also { deleted ->
                if (deleted) {
                    Log.d(TAG, "Removed cached image for book: $bookId")
                }
            }
        } else {
            true
        }
    }

    /**
     * Clears all cached widget images.
     *
     * @return Number of images deleted
     */
    fun clearCache(): Int {
        var count = 0
        cacheDir.listFiles()?.forEach { file ->
            if (file.delete()) {
                count++
            }
        }
        Log.d(TAG, "Cleared $count cached images")
        return count
    }

    /**
     * Gets the cache file for a book ID.
     */
    private fun getCacheFile(bookId: String): File {
        val safeFileName = bookId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(cacheDir, "$safeFileName.jpg")
    }

    /**
     * Saves a bitmap to the cache directory.
     */
    private fun saveBitmapToCache(bookId: String, bitmap: Bitmap): Boolean {
        val file = getCacheFile(bookId)
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
            }
            Log.d(TAG, "Saved bitmap to cache: ${file.absolutePath}")
            true
        } catch (e: IOException) {
            Log.e(TAG, "IO error saving bitmap for book: $bookId", e)
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied saving bitmap for book: $bookId", e)
            false
        }
    }
}
