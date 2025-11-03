@file:Suppress("TooManyFunctions") // Multiple screens and dialogs in one file

package com.darach.openlibrarybooks.feature.settings

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darach.openlibrarybooks.core.common.util.NetworkConnectivity
import com.darach.openlibrarybooks.core.designsystem.component.OfflineIndicator
import com.darach.openlibrarybooks.core.designsystem.component.TriangularPattern
import com.darach.openlibrarybooks.core.designsystem.theme.OpenLibraryTheme
import com.darach.openlibrarybooks.core.domain.model.Settings
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing NetworkConnectivity in SettingsScreen.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsScreenEntryPoint {
    fun networkConnectivity(): NetworkConnectivity
}

/**
 * Settings screen - allows users to customise app preferences.
 *
 * Features:
 * - Change username with validation
 * - Toggle dark mode
 * - Toggle dynamic theme (Android 12+)
 * - View last sync timestamp
 * - Clear cache with confirmation
 * - View app version
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()
    val showClearCacheDialog by viewModel.showClearCacheDialog.collectAsStateWithLifecycle()
    val isClearingCache by viewModel.isClearingCache.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Get NetworkConnectivity using Hilt EntryPoint
    val context = androidx.compose.ui.platform.LocalContext.current
    val networkConnectivity = androidx.compose.runtime.remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SettingsScreenEntryPoint::class.java,
        ).networkConnectivity()
    }

    // Observe network connectivity
    val isOnline by networkConnectivity.observeConnectivity()
        .collectAsStateWithLifecycle(initialValue = true)

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages in Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    SettingsScreenContent(
        modifier = modifier,
        isOffline = !isOnline,
        state = SettingsScreenState(
            settings = settings,
            validationState = validationState,
            showClearCacheDialog = showClearCacheDialog,
            isClearingCache = isClearingCache,
            snackbarHostState = snackbarHostState,
        ),
        actions = SettingsScreenActions(
            onUsernameValidate = viewModel::validateUsername,
            onUsernameUpdate = viewModel::updateUsername,
            onResetValidationState = viewModel::resetValidationState,
            onDarkModeToggle = viewModel::toggleDarkMode,
            onDynamicThemeToggle = viewModel::toggleDynamicTheme,
            onShowClearCacheDialog = viewModel::showClearCacheDialog,
            onDismissClearCacheDialog = viewModel::dismissClearCacheDialog,
            onClearCache = viewModel::clearCache,
            formatLastSyncTimestamp = viewModel::formatLastSyncTimestamp,
        ),
    )
}

/**
 * Data class for grouping settings screen state.
 */
data class SettingsScreenState(
    val settings: Settings,
    val validationState: UsernameValidationState,
    val showClearCacheDialog: Boolean,
    val isClearingCache: Boolean,
    val snackbarHostState: SnackbarHostState,
)

/**
 * Data class for grouping settings screen actions.
 */
data class SettingsScreenActions(
    val onUsernameValidate: (String) -> Unit,
    val onUsernameUpdate: (String) -> Unit,
    val onResetValidationState: () -> Unit,
    val onDarkModeToggle: (Boolean) -> Unit,
    val onDynamicThemeToggle: (Boolean) -> Unit,
    val onShowClearCacheDialog: () -> Unit,
    val onDismissClearCacheDialog: () -> Unit,
    val onClearCache: () -> Unit,
    val formatLastSyncTimestamp: (Long) -> String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(isOffline: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Offline indicator at the top
        OfflineIndicator(
            isOffline = isOffline,
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        )

        // Top bar with triangular pattern
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds(),
        ) {
            // Triangular pattern background with neutral colors
            val neutralPatternColors = listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            )

            TriangularPattern(
                modifier = Modifier.matchParentSize(),
                triangleSize = 120f,
                customColors = neutralPatternColors,
            )

            // TopAppBar with transparent background
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenContent(
    state: SettingsScreenState,
    actions: SettingsScreenActions,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
) {
    var showUsernameDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { SettingsTopBar(isOffline = isOffline) },
        snackbarHost = { SnackbarHost(state.snackbarHostState) },
    ) { paddingValues ->
        SettingsListContent(
            state = state,
            actions = actions,
            onShowUsernameDialog = { showUsernameDialog = true },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }

    // Username Change Dialog
    if (showUsernameDialog) {
        UsernameDialog(
            currentUsername = state.settings.username,
            validationState = state.validationState,
            onDismiss = {
                showUsernameDialog = false
                actions.onResetValidationState()
            },
            onValidate = actions.onUsernameValidate,
            onUpdate = actions.onUsernameUpdate,
        )
    }

    // Clear Cache Confirmation Dialog
    if (state.showClearCacheDialog) {
        ClearCacheDialog(
            onDismiss = actions.onDismissClearCacheDialog,
            onConfirm = actions.onClearCache,
        )
    }
}

/**
 * Scrollable list of settings sections.
 */
@Composable
private fun SettingsListContent(
    state: SettingsScreenState,
    actions: SettingsScreenActions,
    onShowUsernameDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        // User Section
        item {
            UserSection(
                username = state.settings.username,
                onShowUsernameDialog = onShowUsernameDialog,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Appearance Section
        item {
            AppearanceSection(
                darkModeEnabled = state.settings.darkModeEnabled,
                dynamicThemeEnabled = state.settings.dynamicThemeEnabled,
                onDarkModeToggle = actions.onDarkModeToggle,
                onDynamicThemeToggle = actions.onDynamicThemeToggle,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Data Section
        item {
            DataSection(
                lastSyncTimestamp = state.settings.lastSyncTimestamp,
                isClearingCache = state.isClearingCache,
                onShowClearCacheDialog = actions.onShowClearCacheDialog,
                formatLastSyncTimestamp = actions.formatLastSyncTimestamp,
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // About Section
        item { AboutSection() }
    }
}

/**
 * User section with username setting.
 */
@Composable
private fun UserSection(username: String, onShowUsernameDialog: () -> Unit) {
    Column {
        SectionHeader(title = "User", icon = Icons.Outlined.Person)

        ListItem(
            headlineContent = { Text("Username") },
            supportingContent = {
                Text(
                    text = if (username.isBlank()) "Not set" else username,
                )
            },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null)
            },
            trailingContent = {
                OutlinedButton(onClick = onShowUsernameDialog) {
                    Text("Change")
                }
            },
        )
    }
}

/**
 * Appearance section with theme settings.
 */
@Composable
private fun AppearanceSection(
    darkModeEnabled: Boolean,
    dynamicThemeEnabled: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onDynamicThemeToggle: (Boolean) -> Unit,
) {
    Column {
        SectionHeader(title = "Appearance", icon = Icons.Outlined.Palette)

        ListItem(
            headlineContent = { Text("Dark mode") },
            supportingContent = { Text("Use dark theme throughout the app") },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.DarkMode, contentDescription = null)
            },
            trailingContent = {
                Switch(checked = darkModeEnabled, onCheckedChange = onDarkModeToggle)
            },
        )

        // Dynamic theme only on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ListItem(
                headlineContent = { Text("Dynamic theme") },
                supportingContent = { Text("Match colours to your wallpaper") },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = null)
                },
                trailingContent = {
                    Switch(checked = dynamicThemeEnabled, onCheckedChange = onDynamicThemeToggle)
                },
            )
        }
    }
}

/**
 * Data section with sync and cache settings.
 */
@Composable
private fun DataSection(
    lastSyncTimestamp: Long,
    isClearingCache: Boolean,
    onShowClearCacheDialog: () -> Unit,
    formatLastSyncTimestamp: (Long) -> String,
) {
    Column {
        SectionHeader(title = "Data", icon = Icons.Outlined.Sync)

        ListItem(
            headlineContent = { Text("Last sync") },
            supportingContent = { Text(formatLastSyncTimestamp(lastSyncTimestamp)) },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.Sync, contentDescription = null)
            },
        )

        ListItem(
            headlineContent = { Text("Clear cache") },
            supportingContent = { Text("Remove all locally stored books and favourites") },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.DeleteSweep, contentDescription = null)
            },
            trailingContent = {
                if (isClearingCache) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    OutlinedButton(onClick = onShowClearCacheDialog) {
                        Text("Clear")
                    }
                }
            },
        )
    }
}

/**
 * About section with app information.
 */
@Composable
private fun AboutSection() {
    Column {
        SectionHeader(title = "About", icon = Icons.Outlined.Info)

        ListItem(
            headlineContent = { Text("App version") },
            supportingContent = { Text(BuildConfig.VERSION_NAME) },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
            },
        )
    }
}

/**
 * Section header composable for grouping settings.
 */
@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Dialog for changing username with validation.
 */
@Composable
private fun UsernameDialog(
    currentUsername: String,
    validationState: UsernameValidationState,
    onDismiss: () -> Unit,
    onValidate: (String) -> Unit,
    onUpdate: (String) -> Unit,
) {
    var username by rememberSaveable { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change username") },
        text = {
            UsernameDialogContent(
                username = username,
                currentUsername = currentUsername,
                validationState = validationState,
                onUsernameChange = { username = it },
                onValidate = onValidate,
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validationState is UsernameValidationState.Valid) {
                        onUpdate(username)
                        onDismiss()
                    } else {
                        onValidate(username)
                    }
                },
                enabled = username.isNotBlank() &&
                    username != currentUsername &&
                    validationState !is UsernameValidationState.Validating,
            ) {
                Text(
                    if (validationState is UsernameValidationState.Valid) "Save" else "Validate",
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

/**
 * Content of the username dialog.
 */
@Composable
private fun UsernameDialogContent(
    username: String,
    currentUsername: String,
    validationState: UsernameValidationState,
    onUsernameChange: (String) -> Unit,
    onValidate: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column {
        Text(
            text = stringResource(R.string.change_username_prompt),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(R.string.username)) },
            singleLine = true,
            enabled = validationState !is UsernameValidationState.Validating,
            isError = validationState is UsernameValidationState.Invalid,
            supportingText = {
                when (validationState) {
                    is UsernameValidationState.Invalid ->
                        Text(validationState.errorMessage, color = MaterialTheme.colorScheme.error)
                    is UsernameValidationState.Valid ->
                        Text("Username is valid!", color = MaterialTheme.colorScheme.primary)
                    else -> {}
                }
            },
            trailingIcon = {
                when (validationState) {
                    is UsernameValidationState.Validating ->
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    is UsernameValidationState.Valid ->
                        Icon(Icons.Outlined.Check, "Valid", tint = MaterialTheme.colorScheme.primary)
                    is UsernameValidationState.Invalid ->
                        IconButton(onClick = { onUsernameChange("") }) {
                            Icon(Icons.Outlined.Clear, "Clear", tint = MaterialTheme.colorScheme.error)
                        }
                    else -> {}
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (username.isNotBlank() && username != currentUsername) onValidate(username)
                },
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Confirmation dialog for clearing cache.
 */
@Composable
private fun ClearCacheDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
        },
        title = { Text(stringResource(R.string.clear_cache_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.clear_cache_warning),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.clear_cache_items),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.clear_cache_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Clear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(name = "Settings Screen - Light")
@Composable
private fun SettingsScreenPreview() {
    OpenLibraryTheme(darkTheme = false) {
        SettingsScreenContent(
            state = SettingsScreenState(
                settings = Settings(
                    username = "johndoe",
                    darkModeEnabled = false,
                    dynamicThemeEnabled = true,
                    lastSyncTimestamp = System.currentTimeMillis(),
                ),
                validationState = UsernameValidationState.Idle,
                showClearCacheDialog = false,
                isClearingCache = false,
                snackbarHostState = remember { SnackbarHostState() },
            ),
            actions = SettingsScreenActions(
                onUsernameValidate = {},
                onUsernameUpdate = {},
                onResetValidationState = {},
                onDarkModeToggle = {},
                onDynamicThemeToggle = {},
                onShowClearCacheDialog = {},
                onDismissClearCacheDialog = {},
                onClearCache = {},
                formatLastSyncTimestamp = { "3 hours ago" },
            ),
            isOffline = false,
        )
    }
}

@Preview(name = "Username Dialog")
@Composable
private fun UsernameDialogPreview() {
    OpenLibraryTheme {
        UsernameDialog(
            currentUsername = "johndoe",
            validationState = UsernameValidationState.Invalid("Username not found on Open Library"),
            onDismiss = {},
            onValidate = {},
            onUpdate = {},
        )
    }
}

@Preview(name = "Clear Cache Dialog")
@Composable
private fun ClearCacheDialogPreview() {
    OpenLibraryTheme {
        ClearCacheDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}
