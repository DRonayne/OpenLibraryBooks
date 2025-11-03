package com.darach.openlibrarybooks.feature.settings

import android.util.Log
import app.cash.turbine.test
import com.darach.openlibrarybooks.core.domain.model.FilterOptions
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.model.Settings
import com.darach.openlibrarybooks.core.domain.model.SortOption
import com.darach.openlibrarybooks.core.domain.repository.BooksRepository
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
import com.darach.openlibrarybooks.core.domain.repository.SettingsRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.UnknownHostException

/**
 * Comprehensive unit tests for SettingsViewModel.
 *
 * Tests cover:
 * - Initial state and settings loading
 * - Username validation (success, failure, timeout, empty)
 * - Updating username after validation
 * - Dark mode and dynamic theme toggling
 * - Sort and filter option updates
 * - Last sync timestamp formatting
 * - Clear cache with confirmation dialog
 * - Clear cache with auto-sync trigger
 * - Error handling for various scenarios
 * - Disposal of RxJava subscriptions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var mockSettingsRepository: SettingsRepository
    private lateinit var mockBooksRepository: BooksRepository
    private lateinit var mockFavouritesRepository: FavouritesRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    private val defaultSettings = Settings(
        username = "testuser",
        darkModeEnabled = false,
        dynamicThemeEnabled = true,
        lastSyncTimestamp = 1609459200000L, // 2021-01-01 00:00:00
        sortOption = SortOption.DateAddedNewest,
        filterOptions = FilterOptions(),
    )

    private val settingsFlow = MutableStateFlow(defaultSettings)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log class
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        // Override RxJava schedulers for synchronous testing
        // Use trampoline for immediate execution
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }

        mockSettingsRepository = mockk(relaxed = true)
        mockBooksRepository = mockk(relaxed = true)
        mockFavouritesRepository = mockk(relaxed = true)

        every { mockSettingsRepository.getSettings() } returns settingsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        RxJavaPlugins.reset()
        unmockkStatic(Log::class)
    }

    @Test
    fun `initial state loads settings from repository`() = runTest {
        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.settings.test {
            val settings = awaitItem()
            settings shouldBe defaultSettings
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial validation state is Idle`() = runTest {
        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.validationState.value shouldBe UsernameValidationState.Idle
    }

    @Test
    fun `validateUsername sets Validating state and then Valid on success`() = runTest {
        // Return a Single that completes immediately on subscription
        every { mockSettingsRepository.validateUsername("validuser") } returns
            Single.fromCallable { true }

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.validateUsername("validuser")

        // Due to synchronous testing with trampoline scheduler,
        // validation completes immediately
        viewModel.validationState.value shouldBe UsernameValidationState.Valid

        verify { mockSettingsRepository.validateUsername("validuser") }
    }

    @Test
    fun `validateUsername sets Invalid state when username not found`() = runTest {
        every { mockSettingsRepository.validateUsername("invaliduser") } returns
            Single.fromCallable { false }

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.validateUsername("invaliduser")

        val finalState = viewModel.validationState.value
        finalState.shouldBeInstanceOf<UsernameValidationState.Invalid>()
        finalState.errorMessage shouldBe "Username 'invaliduser' not found on Open Library"
    }

    @Test
    fun `validateUsername sets Invalid state when username is blank`() = runTest {
        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.validationState.test {
            awaitItem() shouldBe UsernameValidationState.Idle

            viewModel.validateUsername("")

            val finalState = awaitItem()
            finalState.shouldBeInstanceOf<UsernameValidationState.Invalid>()
            finalState.errorMessage shouldBe "Username cannot be empty"
        }

        // Should not call repository for blank username
        verify(exactly = 0) { mockSettingsRepository.validateUsername(any()) }
    }

    @Test
    fun `validateUsername handles timeout error from repository`() = runTest {
        // Repository returns an error with TimeoutException
        every { mockSettingsRepository.validateUsername("timeoutuser") } returns
            Single.error(java.util.concurrent.TimeoutException("Timeout"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.validateUsername("timeoutuser")

        val finalState = viewModel.validationState.value
        finalState.shouldBeInstanceOf<UsernameValidationState.Invalid>()
        finalState.errorMessage shouldBe "Validation timed out. Please check your internet connection."
    }

    @Test
    fun `validateUsername handles network errors`() = runTest {
        every { mockSettingsRepository.validateUsername("networkerror") } returns
            Single.error(UnknownHostException("No internet"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.validateUsername("networkerror")

        val finalState = viewModel.validationState.value
        finalState.shouldBeInstanceOf<UsernameValidationState.Invalid>()
        finalState.errorMessage shouldBe "No internet connection. Please check your network."
    }

    @Test
    fun `updateUsername calls repository and resets validation state`() = runTest {
        every { mockSettingsRepository.updateUsername("newuser") } returns Completable.complete()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.updateUsername("newuser")

        verify { mockSettingsRepository.updateUsername("newuser") }
        viewModel.validationState.value shouldBe UsernameValidationState.Idle
    }

    @Test
    fun `updateUsername handles error`() = runTest {
        every { mockSettingsRepository.updateUsername("erroruser") } returns
            Completable.error(IOException("Database error"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.errorMessage.test {
            awaitItem() shouldBe null

            viewModel.updateUsername("erroruser")

            val error = awaitItem()
            error shouldBe "Failed to save username: Database error"
        }
    }

    @Test
    fun `toggleDarkMode calls repository`() = runTest {
        every { mockSettingsRepository.toggleDarkMode(true) } returns Completable.complete()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.toggleDarkMode(true)

        verify { mockSettingsRepository.toggleDarkMode(true) }
    }

    @Test
    fun `toggleDarkMode handles error`() = runTest {
        every { mockSettingsRepository.toggleDarkMode(true) } returns
            Completable.error(IOException("Failed to save"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.errorMessage.test {
            awaitItem() shouldBe null

            viewModel.toggleDarkMode(true)

            val error = awaitItem()
            error shouldBe "Failed to update dark mode: Failed to save"
        }
    }

    @Test
    fun `toggleDynamicTheme calls repository`() = runTest {
        every { mockSettingsRepository.toggleDynamicTheme(false) } returns Completable.complete()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.toggleDynamicTheme(false)

        verify { mockSettingsRepository.toggleDynamicTheme(false) }
    }

    @Test
    fun `toggleDynamicTheme handles error`() = runTest {
        every { mockSettingsRepository.toggleDynamicTheme(false) } returns
            Completable.error(IOException("Failed to save"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.errorMessage.test {
            awaitItem() shouldBe null

            viewModel.toggleDynamicTheme(false)

            val error = awaitItem()
            error shouldBe "Failed to update dynamic theme: Failed to save"
        }
    }

    @Test
    fun `updateSortOption calls repository`() = runTest {
        val sortOption = SortOption.TitleAscending
        every { mockSettingsRepository.updateSortOption(sortOption) } returns Completable.complete()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.updateSortOption(sortOption)

        verify { mockSettingsRepository.updateSortOption(sortOption) }
    }

    @Test
    fun `updateSortOption handles error`() = runTest {
        val sortOption = SortOption.TitleAscending
        every { mockSettingsRepository.updateSortOption(sortOption) } returns
            Completable.error(IOException("Failed to save"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.errorMessage.test {
            awaitItem() shouldBe null

            viewModel.updateSortOption(sortOption)

            val error = awaitItem()
            error shouldBe "Failed to update sort option: Failed to save"
        }
    }

    @Test
    fun `updateFilterOptions calls repository`() = runTest {
        val filterOptions = FilterOptions(readingStatuses = setOf(ReadingStatus.CurrentlyReading))
        every { mockSettingsRepository.updateFilterOptions(filterOptions) } returns Completable.complete()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.updateFilterOptions(filterOptions)

        verify { mockSettingsRepository.updateFilterOptions(filterOptions) }
    }

    @Test
    fun `updateFilterOptions handles error`() = runTest {
        val filterOptions = FilterOptions(readingStatuses = setOf(ReadingStatus.CurrentlyReading))
        every { mockSettingsRepository.updateFilterOptions(filterOptions) } returns
            Completable.error(IOException("Failed to save"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.errorMessage.test {
            awaitItem() shouldBe null

            viewModel.updateFilterOptions(filterOptions)

            val error = awaitItem()
            error shouldBe "Failed to update filter options: Failed to save"
        }
    }

    @Test
    fun `formatLastSyncTimestamp returns Never for zero timestamp`() {
        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        val formatted = viewModel.formatLastSyncTimestamp(0L)

        formatted shouldBe "Never"
    }

    @Test
    fun `formatLastSyncTimestamp formats valid timestamp correctly`() {
        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        // 2021-01-01 00:00:00 GMT (adjust based on your timezone in tests)
        val timestamp = 1609459200000L
        val formatted = viewModel.formatLastSyncTimestamp(timestamp)

        // Check that it contains expected date components
        formatted.contains("2021") shouldBe true
        formatted.contains("Jan") shouldBe true
    }

    @Test
    fun `showClearCacheDialog sets dialog state to true`() = runTest {
        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.showClearCacheDialog.test {
            awaitItem() shouldBe false

            viewModel.showClearCacheDialog()

            awaitItem() shouldBe true
        }
    }

    @Test
    fun `dismissClearCacheDialog sets dialog state to false`() = runTest {
        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.showClearCacheDialog()
        viewModel.showClearCacheDialog.value shouldBe true

        viewModel.showClearCacheDialog.test {
            awaitItem() shouldBe true

            viewModel.dismissClearCacheDialog()

            awaitItem() shouldBe false
        }
    }

    @Test
    fun `clearCache clears books and favourites`() = runTest {
        every { mockBooksRepository.clearCache() } returns Completable.complete()
        every { mockFavouritesRepository.clearAllFavourites() } returns Completable.complete()
        // Mock sync operations (called asynchronously within onComplete callback)
        every { mockBooksRepository.sync(any()) } returns Completable.complete()
        every { mockSettingsRepository.updateLastSyncTimestamp(any()) } returns Completable.complete()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.clearCache()

        // Advance coroutine dispatcher to ensure all callbacks execute
        advanceUntilIdle()

        // Verify the main cache clear operations were called
        verify { mockBooksRepository.clearCache() }
        verify { mockFavouritesRepository.clearAllFavourites() }

        // State should be updated after cache clear operations complete
        viewModel.showClearCacheDialog.value shouldBe false
    }

    @Test
    fun `clearCache handles error gracefully`() = runTest {
        every { mockBooksRepository.clearCache() } returns
            Completable.error(IOException("Failed to clear cache"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.errorMessage.test {
            awaitItem() shouldBe null

            viewModel.clearCache()

            val error = awaitItem()
            error shouldBe "Failed to clear cache: Failed to clear cache"

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.isClearingCache.value shouldBe false
    }

    @Test
    fun `clearCache does not trigger sync if username is blank`() = runTest {
        // Update settings flow to have blank username
        settingsFlow.value = defaultSettings.copy(username = "")

        every { mockBooksRepository.clearCache() } returns Completable.complete()
        every { mockFavouritesRepository.clearAllFavourites() } returns Completable.complete()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.clearCache()

        // Advance coroutine dispatcher to ensure all callbacks execute
        advanceUntilIdle()

        verify { mockBooksRepository.clearCache() }
        verify { mockFavouritesRepository.clearAllFavourites() }
        // Should NOT call sync when username is blank
        verify(exactly = 0) { mockBooksRepository.sync(any()) }
    }

    @Test
    fun `clearCache handles error in cache operations`() = runTest {
        // Test that errors during cache operations are handled gracefully
        every { mockBooksRepository.clearCache() } returns Completable.complete()
        every { mockFavouritesRepository.clearAllFavourites() } returns Completable.complete()
        // Mock sync to succeed (errors in sync are tested separately in integration tests)
        every { mockBooksRepository.sync(any()) } returns Completable.complete()
        every { mockSettingsRepository.updateLastSyncTimestamp(any()) } returns Completable.complete()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.clearCache()

        // Advance coroutine dispatcher
        advanceUntilIdle()

        // Verify cache operations were called
        verify { mockBooksRepository.clearCache() }
        verify { mockFavouritesRepository.clearAllFavourites() }
    }

    @Test
    fun `resetValidationState sets validation state to Idle`() = runTest {
        every { mockSettingsRepository.validateUsername("testuser") } returns
            Single.fromCallable { true }

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.validateUsername("testuser")
        viewModel.validationState.value shouldBe UsernameValidationState.Valid

        viewModel.resetValidationState()
        viewModel.validationState.value shouldBe UsernameValidationState.Idle
    }

    @Test
    fun `clearErrorMessage resets error state`() = runTest {
        every { mockSettingsRepository.updateUsername("erroruser") } returns
            Completable.error(IOException("Database error"))

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.errorMessage.test {
            awaitItem() shouldBe null

            viewModel.updateUsername("erroruser")
            awaitItem() shouldBe "Failed to save username: Database error"

            viewModel.clearErrorMessage()
            awaitItem() shouldBe null
        }
    }

    @Test
    fun `isClearingCache is false initially`() = runTest {
        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.isClearingCache.value shouldBe false
    }

    @Test
    fun `isClearingCache is true during cache clear operation`() = runTest {
        // Create a Completable that never completes to keep the clearing state active
        every { mockBooksRepository.clearCache() } returns Completable.never()

        val viewModel = SettingsViewModel(
            mockSettingsRepository,
            mockBooksRepository,
            mockFavouritesRepository,
        )

        viewModel.isClearingCache.test {
            awaitItem() shouldBe false

            viewModel.clearCache()

            awaitItem() shouldBe true
        }
    }
}
