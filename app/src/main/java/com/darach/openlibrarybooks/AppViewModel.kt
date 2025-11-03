package com.darach.openlibrarybooks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.openlibrarybooks.core.domain.model.Settings
import com.darach.openlibrarybooks.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the main app activity.
 *
 * Provides access to app-wide settings such as dark mode and dynamic theming,
 * which need to be observed at the root level to apply themes correctly.
 *
 * @property settingsRepository Repository for accessing app settings
 */
@HiltViewModel
class AppViewModel @Inject constructor(settingsRepository: SettingsRepository) : ViewModel() {

    /**
     * StateFlow exposing the current app settings.
     *
     * Used by MainActivity to observe dark mode and dynamic theme preferences
     * and apply them to the app's theme.
     */
    val settings: StateFlow<Settings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings(),
        )
}
