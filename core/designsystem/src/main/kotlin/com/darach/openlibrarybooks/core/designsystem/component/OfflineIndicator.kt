package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme

/**
 * Minimal banner indicator that displays when the app is offline.
 *
 * Features:
 * - Warning colour background
 * - Offline icon and text
 * - Smooth slide-in/out animation from top
 * - Automatically shows/hides based on offline state
 *
 * @param isOffline Whether the app is currently offline
 * @param modifier Modifier to be applied to the indicator
 */
@Composable
fun OfflineIndicator(isOffline: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )

                Text(
                    text = "Offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Preview(name = "Offline Indicator - Visible - Light", showBackground = true)
@Composable
private fun OfflineIndicatorVisiblePreviewLight() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        OfflineIndicator(isOffline = true)
    }
}

@Preview(name = "Offline Indicator - Visible - Dark", showBackground = true)
@Composable
private fun OfflineIndicatorVisiblePreviewDark() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        OfflineIndicator(isOffline = true)
    }
}

@Preview(name = "Offline Indicator - Hidden", showBackground = true)
@Composable
private fun OfflineIndicatorHiddenPreview() {
    OpenLibraryTheme {
        OfflineIndicator(isOffline = false)
    }
}

@Preview(name = "Offline Indicator - Interactive Demo", showBackground = true)
@Composable
private fun OfflineIndicatorInteractivePreview() {
    var isOffline by remember { mutableStateOf(true) }

    OpenLibraryTheme {
        Box {
            OfflineIndicator(isOffline = isOffline)
        }
    }
}
