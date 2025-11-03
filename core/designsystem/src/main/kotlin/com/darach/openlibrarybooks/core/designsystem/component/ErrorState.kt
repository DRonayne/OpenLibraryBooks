package com.darach.openlibrarybooks.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.openlibrarybooks.core.designsystem.R
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import kotlinx.coroutines.delay

/**
 * Full-screen error state component with icon, message, and retry button.
 *
 * Features:
 * - Different error types with contextual icons and messages
 * - Fade-in animation when displayed
 * - Optional retry button with callback
 * - Theme-adaptive colours
 *
 * @param message The error message to display
 * @param modifier Modifier to be applied to the error state container
 * @param errorType The type of error for contextual icon and styling
 * @param onRetry Optional callback when the retry button is clicked. If null, no button is shown.
 */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    errorType: ErrorType = ErrorType.GENERIC,
    onRetry: (() -> Unit)? = null,
) {
    var visible by remember { mutableStateOf(false) }

    // Trigger fade-in animation
    LaunchedEffect(Unit) {
        delay(50) // Small delay for smoother appearance
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Error icon based on error type
            Icon(
                imageVector = when (errorType) {
                    ErrorType.NETWORK -> Icons.Default.WifiOff
                    ErrorType.TIMEOUT -> Icons.Default.HourglassEmpty
                    ErrorType.SERVER -> Icons.Default.CloudOff
                    ErrorType.GENERIC -> Icons.Default.ErrorOutline
                },
                contentDescription = stringResource(R.string.error_icon),
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            // Retry button (if callback is provided)
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

/**
 * Types of errors that can be displayed in the error state.
 */
enum class ErrorType {
    /**
     * Generic error (default icon and messaging)
     */
    GENERIC,

    /**
     * Network connectivity error (no internet)
     */
    NETWORK,

    /**
     * Timeout error (request took too long)
     */
    TIMEOUT,

    /**
     * Server/API error (service unavailable)
     */
    SERVER,
}

/**
 * Convenience composable for network error state.
 *
 * @param onRetry Callback when the retry button is clicked
 * @param modifier Modifier to be applied to the error state
 */
@Composable
fun NetworkErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ErrorState(
        message = stringResource(R.string.error_no_internet),
        modifier = modifier,
        errorType = ErrorType.NETWORK,
        onRetry = onRetry,
    )
}

/**
 * Convenience composable for timeout error state.
 *
 * @param onRetry Callback when the retry button is clicked
 * @param modifier Modifier to be applied to the error state
 */
@Composable
fun TimeoutErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ErrorState(
        message = stringResource(R.string.error_timeout),
        modifier = modifier,
        errorType = ErrorType.TIMEOUT,
        onRetry = onRetry,
    )
}

/**
 * Convenience composable for server error state.
 *
 * @param onRetry Callback when the retry button is clicked
 * @param modifier Modifier to be applied to the error state
 */
@Composable
fun ServerErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ErrorState(
        message = stringResource(R.string.error_server_unavailable),
        modifier = modifier,
        errorType = ErrorType.SERVER,
        onRetry = onRetry,
    )
}

@Preview(name = "Error State - Generic - Light", showBackground = true)
@Composable
private fun ErrorStateGenericPreviewLight() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        ErrorState(
            message = "Something went wrong. Please try again.",
            errorType = ErrorType.GENERIC,
            onRetry = {},
        )
    }
}

@Preview(name = "Error State - Generic - Dark", showBackground = true)
@Composable
private fun ErrorStateGenericPreviewDark() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        ErrorState(
            message = "Something went wrong. Please try again.",
            errorType = ErrorType.GENERIC,
            onRetry = {},
        )
    }
}

@Preview(name = "Error State - Network - Light", showBackground = true)
@Composable
private fun ErrorStateNetworkPreviewLight() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        NetworkErrorState(
            onRetry = {},
        )
    }
}

@Preview(name = "Error State - Server - Dark", showBackground = true)
@Composable
private fun ErrorStateServerPreviewDark() {
    OpenLibraryTheme(darkTheme = true, dynamicColor = false) {
        ServerErrorState(
            onRetry = {},
        )
    }
}

@Preview(name = "Error State - Timeout - Light", showBackground = true)
@Composable
private fun ErrorStateTimeoutPreviewLight() {
    OpenLibraryTheme(darkTheme = false, dynamicColor = false) {
        TimeoutErrorState(
            onRetry = {},
        )
    }
}

@Preview(name = "Error State - No Retry Button", showBackground = true)
@Composable
private fun ErrorStateNoRetryPreview() {
    OpenLibraryTheme {
        ErrorState(
            message = "An error occurred",
            errorType = ErrorType.GENERIC,
            onRetry = null, // No retry button
        )
    }
}
