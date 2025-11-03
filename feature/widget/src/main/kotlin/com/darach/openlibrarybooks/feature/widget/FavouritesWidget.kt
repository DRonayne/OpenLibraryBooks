package com.darach.openlibrarybooks.feature.widget

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.darach.openlibrarybooks.core.domain.model.Book
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

/**
 * Glance widget displaying the 3 most recent favourite books.
 * Shows book covers in a horizontal row with clickable action to open favourites screen.
 */
class FavouritesWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "FavouritesWidget"
        const val DEEPLINK_URI = "openlibrarybooks://favourites"
        const val WIDGET_ACTION = "com.darach.openlibrarybooks.WIDGET_FAVOURITES"
    }

    /**
     * Hilt EntryPoint for accessing dependencies in non-Hilt context (Glance widget).
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FavouritesWidgetEntryPoint {
        fun widgetRepository(): WidgetRepository
        fun widgetImageCache(): WidgetImageCache
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "Updating widget content")

        // Access dependencies via Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            FavouritesWidgetEntryPoint::class.java,
        )
        val repository = entryPoint.widgetRepository()
        val imageCache = entryPoint.widgetImageCache()

        // Fetch the 3 most recent favourites synchronously for widget
        val favourites = runBlocking {
            repository.getRecentFavourites()
                .blockingFirst(emptyList())
        }

        Log.d(TAG, "Loaded ${favourites.size} favourites")

        // Cache images for any favourites that don't have cached covers yet
        favourites.forEach { book ->
            val coverUrl = book.coverUrl
            if (coverUrl != null && !imageCache.isCached(book.id)) {
                Log.d(TAG, "Caching cover for book: ${book.title}")
                imageCache.cacheImage(book.id, coverUrl)
            } else if (imageCache.isCached(book.id)) {
                Log.d(TAG, "Cover already cached for book: ${book.title}")
            }
        }

        provideContent {
            GlanceTheme {
                FavouritesWidgetContent(
                    context = context,
                    favourites = favourites,
                    imageCache = imageCache,
                )
            }
        }
    }
}

/**
 * Main content composable for the favourites widget.
 *
 * @param context Android context for creating intents
 * @param favourites List of favourite books to display (up to 3)
 * @param imageCache Cache for loading book cover images
 */
@Composable
private fun FavouritesWidgetContent(context: Context, favourites: List<Book>, imageCache: WidgetImageCache) {
    // Use WidgetLauncherActivity as trampoline to launch MainActivity with favourites deeplink
    val componentName = ComponentName(
        context.packageName,
        "com.darach.openlibrarybooks.feature.widget.WidgetLauncherActivity",
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickable(actionStartActivity(componentName)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
        ) {
            // Widget header
            Text(
                text = "My Favourites",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF006B2E)), // Primary green
                ),
            )
            Spacer(modifier = GlanceModifier.height(12.dp))

            // Content
            if (favourites.isEmpty()) {
                EmptyStateContent()
            } else {
                FavouriteBooksRow(
                    favourites = favourites,
                    imageCache = imageCache,
                )
            }
        }
    }
}

/**
 * Displays empty state when there are no favourite books.
 */
@Composable
private fun EmptyStateContent() {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Empty state icon using text emoji
            Text(
                text = "\uD83D\uDCD6", // Book emoji
                style = TextStyle(
                    fontSize = 48.sp,
                ),
            )
            Spacer(modifier = GlanceModifier.height(12.dp))

            // Empty state text
            Text(
                text = "No favourites yet",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onBackground,
                ),
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Tap to add your first book",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = ColorProvider(Color(0xFF3E4A3E)), // On surface variant
                ),
            )
        }
    }
}

/**
 * Displays a horizontal row of favourite book covers with titles.
 *
 * @param favourites List of books to display (max 3)
 * @param imageCache Cache for loading book cover images
 */
@Composable
private fun FavouriteBooksRow(favourites: List<Book>, imageCache: WidgetImageCache) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Top,
    ) {
        favourites.take(3).forEachIndexed { index, book ->
            BookCoverItem(
                book = book,
                imageCache = imageCache,
            )

            // Add spacing between covers (but not after the last one)
            if (index < favourites.size - 1 && index < 2) {
                Spacer(modifier = GlanceModifier.width(8.dp))
            }
        }
    }
}

/**
 * Displays a single book cover with title.
 *
 * @param book The book to display
 * @param imageCache Cache for loading book cover images
 */
@Composable
private fun BookCoverItem(book: Book, imageCache: WidgetImageCache) {
    val cachedBitmap = imageCache.getCachedImage(book.id)
    Log.d(
        "FavouritesWidget",
        "BookCoverItem for '${book.title}' (id: ${book.id}): bitmap = ${if (cachedBitmap != null) "found" else "null"}",
    )

    Column(
        modifier = GlanceModifier.width(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Book cover
        Box(
            modifier = GlanceModifier
                .size(width = 90.dp, height = 120.dp)
                .cornerRadius(4.dp)
                .background(ColorProvider(Color(0xFFD9E6D6))), // Surface variant light
            contentAlignment = Alignment.Center,
        ) {
            if (cachedBitmap != null) {
                // Display cached cover image
                Image(
                    provider = ImageProvider(cachedBitmap),
                    contentDescription = "Cover for ${book.title}",
                    modifier = GlanceModifier
                        .size(width = 90.dp, height = 120.dp)
                        .cornerRadius(4.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                // Fallback to styled initials
                BookCoverFallback(title = book.title)
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Book title (truncated to 2 lines)
        Text(
            text = book.title,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onBackground,
                textAlign = TextAlign.Center,
            ),
            maxLines = 2,
        )
    }
}

/**
 * Fallback composable showing book title initials when cover is unavailable.
 *
 * @param title The book title to extract initials from
 */
@Composable
private fun BookCoverFallback(title: String) {
    val initials = extractTitleInitials(title)
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color(0xFF006B2E)), // Primary green
            ),
        )
    }
}

/**
 * Extract up to 2 initials from a book title.
 * Takes the first letter of the first two words.
 *
 * Examples:
 * - "The Hobbit" -> "TH"
 * - "1984" -> "19"
 * - "A" -> "A"
 *
 * @param title Book title
 * @return Title initials (1-2 characters)
 */
private fun extractTitleInitials(title: String): String {
    val words = title.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }

    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> {
            val first = words[0].first().uppercase()
            val second = words[1].first().uppercase()
            "$first$second"
        }
    }
}
