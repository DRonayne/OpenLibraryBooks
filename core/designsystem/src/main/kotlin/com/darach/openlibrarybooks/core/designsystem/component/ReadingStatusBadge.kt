package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus

/**
 * Displays a coloured badge showing the reading status of a book.
 *
 * Maps [ReadingStatus] to user-friendly text:
 * - WantToRead → "Want to Read"
 * - CurrentlyReading → "Reading"
 * - AlreadyRead → "Finished"
 *
 * @param readingStatus The reading status to display
 * @param modifier Optional modifier for the badge
 */
@Composable
fun ReadingStatusBadge(readingStatus: ReadingStatus, modifier: Modifier = Modifier) {
    Text(
        text = when (readingStatus) {
            ReadingStatus.WantToRead -> "Want to Read"
            ReadingStatus.CurrentlyReading -> "Reading"
            ReadingStatus.AlreadyRead -> "Finished"
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Preview(name = "Want to Read")
@Composable
private fun ReadingStatusBadgeWantToReadPreview() {
    OpenLibraryTheme {
        ReadingStatusBadge(readingStatus = ReadingStatus.WantToRead)
    }
}

@Preview(name = "Currently Reading")
@Composable
private fun ReadingStatusBadgeCurrentlyReadingPreview() {
    OpenLibraryTheme {
        ReadingStatusBadge(readingStatus = ReadingStatus.CurrentlyReading)
    }
}

@Preview(name = "Already Read")
@Composable
private fun ReadingStatusBadgeAlreadyReadPreview() {
    OpenLibraryTheme {
        ReadingStatusBadge(readingStatus = ReadingStatus.AlreadyRead)
    }
}

@Preview(name = "Dark - Want to Read")
@Composable
private fun ReadingStatusBadgeDarkPreview() {
    OpenLibraryTheme(darkTheme = true) {
        ReadingStatusBadge(readingStatus = ReadingStatus.WantToRead)
    }
}
