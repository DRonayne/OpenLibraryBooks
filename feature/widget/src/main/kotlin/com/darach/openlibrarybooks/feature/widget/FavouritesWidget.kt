package com.darach.openlibrarybooks.feature.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
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
        const val DEEPLINK_URI = "openlibrarybooks://favourites"
    }

    /**
     * Hilt EntryPoint for accessing dependencies in non-Hilt context (Glance widget).
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FavouritesWidgetEntryPoint {
        fun widgetRepository(): WidgetRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Access WidgetRepository via Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            FavouritesWidgetEntryPoint::class.java,
        )
        val repository = entryPoint.widgetRepository()

        // Fetch the 3 most recent favourites synchronously for widget
        val favourites = runBlocking {
            repository.getRecentFavourites()
                .blockingFirst(emptyList())
        }

        provideContent {
            GlanceTheme {
                FavouritesWidgetContent(
                    context = context,
                    favourites = favourites,
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
 */
@Composable
private fun FavouritesWidgetContent(context: Context, favourites: List<Book>) {
    // Create ComponentName for MainActivity to handle deeplink
    val componentName = ComponentName(
        context.packageName,
        "com.darach.openlibrarybooks.MainActivity",
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)
            .clickable(actionStartActivity(componentName)),
        contentAlignment = Alignment.Center,
    ) {
        if (favourites.isEmpty()) {
            EmptyStateContent()
        } else {
            FavouriteBooksRow(favourites = favourites)
        }
    }
}

/**
 * Displays empty state when there are no favourite books.
 */
@Composable
private fun EmptyStateContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "No favourites yet",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onBackground,
            ),
        )
        Spacer(modifier = GlanceModifier.size(4.dp))
        Text(
            text = "Tap to add your first book",
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant,
            ),
        )
    }
}

/**
 * Displays a horizontal row of favourite book covers.
 *
 * @param favourites List of books to display (max 3)
 */
@Composable
private fun FavouriteBooksRow(favourites: List<Book>) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        favourites.take(3).forEachIndexed { index, book ->
            BookCoverItem(book = book)

            // Add spacing between covers (but not after the last one)
            if (index < favourites.size - 1 && index < 2) {
                Spacer(modifier = GlanceModifier.width(12.dp))
            }
        }
    }
}

/**
 * Displays a single book cover or fallback with title initials.
 *
 * @param book The book to display
 */
@Composable
private fun BookCoverItem(book: Book) {
    Box(
        modifier = GlanceModifier
            .size(width = 60.dp, height = 80.dp)
            .background(GlanceTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (book.coverUrl != null) {
            // TODO: Glance doesn't support Coil directly. For now, we'll show initials.
            // In a production app, you'd pre-download images or use RemoteViews with Coil.
            BookCoverFallback(title = book.title)
        } else {
            BookCoverFallback(title = book.title)
        }
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
    Text(
        text = initials,
        style = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GlanceTheme.colors.onSurfaceVariant,
        ),
    )
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
