package com.darach.openlibrarybooks.core.domain.repository

import com.darach.openlibrarybooks.core.domain.model.Settings
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user settings and preferences.
 *
 * Handles app settings like username, theme preferences, and sync timestamps.
 * Uses RxJava for reactive operations and Flow for observing settings changes.
 * Settings are persisted locally using DataStore Preferences.
 */
interface SettingsRepository {

    /**
     * Observe user settings.
     *
     * Returns a Flow that emits the current settings whenever any setting
     * value changes. This allows the UI to reactively update when settings change.
     *
     * @return Flow emitting the current settings
     */
    fun getSettings(): Flow<Settings>

    /**
     * Update the Open Library username.
     *
     * Sets the username used for fetching reading lists from Open Library.
     * The username should be validated using [validateUsername] before updating.
     *
     * @param username The new Open Library username
     * @return Completable that completes when the username is updated
     */
    fun updateUsername(username: String): Completable

    /**
     * Validate that a username exists on Open Library.
     *
     * Makes an API call to verify that the username is valid by attempting
     * to fetch the user's reading lists. This should be called before updating
     * the username to ensure it's valid.
     *
     * @param username The username to validate
     * @return Single emitting true if the username is valid, false otherwise
     */
    fun validateUsername(username: String): Single<Boolean>

    /**
     * Toggle dark mode on or off.
     *
     * Enables or disables dark mode for the app. When enabled, the app will
     * use dark theme colours.
     *
     * @param enabled True to enable dark mode, false to disable
     * @return Completable that completes when the setting is updated
     */
    fun toggleDarkMode(enabled: Boolean): Completable

    /**
     * Toggle dynamic theming (Material You) on or off.
     *
     * Enables or disables dynamic colour theming based on the device wallpaper.
     * Only available on Android 12 and above.
     *
     * @param enabled True to enable dynamic theming, false to disable
     * @return Completable that completes when the setting is updated
     */
    fun toggleDynamicTheme(enabled: Boolean): Completable

    /**
     * Observe the last sync timestamp.
     *
     * Returns a Flow that emits the timestamp (in milliseconds) of the last
     * successful sync with the Open Library API.
     *
     * @return Flow emitting the last sync timestamp
     */
    fun getLastSyncTimestamp(): Flow<Long>

    /**
     * Update the last sync timestamp.
     *
     * Records the timestamp of the most recent successful sync operation.
     * This is typically called after completing a background sync.
     *
     * @param timestamp The sync timestamp in milliseconds since epoch
     * @return Completable that completes when the timestamp is updated
     */
    fun updateLastSyncTimestamp(timestamp: Long): Completable

    /**
     * Clear all settings and reset to defaults.
     *
     * Removes all stored settings and reverts to default values. This operation
     * cannot be undone.
     *
     * @return Completable that completes when all settings are cleared
     */
    fun clearSettings(): Completable
}
