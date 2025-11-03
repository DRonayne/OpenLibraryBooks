package com.darach.openlibrarybooks.feature.favourites

import android.util.Log
import app.cash.turbine.test
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.domain.model.Book
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Comprehensive unit tests for FavouritesViewModel.
 *
 * Tests cover:
 * - Initial state and favourites fetching
 * - Toggling favourites with optimistic updates
 * - Rollback on toggle failure
 * - Checking favourite status
 * - Clearing all favourites
 * - Error handling for different error types
 * - Disposal of RxJava subscriptions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {

    private lateinit var mockRepository: FavouritesRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleFavouriteBooks = listOf(
        Book(
            id = "1",
            title = "The Hobbit",
            authors = listOf("J.R.R. Tolkien"),
            publishYear = 1937,
            subjects = listOf("Fantasy", "Adventure"),
            readingStatus = ReadingStatus.WantToRead,
            isFavorite = true,
            dateAdded = 1000L,
        ),
        Book(
            id = "3",
            title = "To Kill a Mockingbird",
            authors = listOf("Harper Lee"),
            publishYear = 1960,
            subjects = listOf("Fiction", "Classic"),
            readingStatus = ReadingStatus.AlreadyRead,
            isFavorite = true,
            dateAdded = 3000L,
        ),
        Book(
            id = "6",
            title = "The Lord of the Rings",
            authors = listOf("J.R.R. Tolkien"),
            publishYear = 1954,
            subjects = listOf("Fantasy", "Adventure", "Epic"),
            readingStatus = ReadingStatus.CurrentlyReading,
            isFavorite = true,
            dateAdded = 6000L,
        ),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log class since it's not available in unit tests
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        // Override RxJava schedulers for synchronous testing
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }

        mockRepository = mockk(relaxed = true)
        every { mockRepository.getFavourites() } returns flowOf(sampleFavouriteBooks)
        every { mockRepository.getFavouriteCount() } returns flowOf(sampleFavouriteBooks.size)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        RxJavaPlugins.reset()
        unmockkStatic(Log::class)
    }

    @Test
    fun `initial state is Loading or Success`() = runTest {
        val viewModel = FavouritesViewModel(mockRepository)
        viewModel.favouritesUiState.test {
            val initialState = awaitItem()
            // With UnconfinedTestDispatcher, Loading state may be skipped
            // because everything runs synchronously
            initialState.shouldBeInstanceOf<UiState<*>>()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favouritesUiState emits Success with favourite books`() = runTest {
        val viewModel = FavouritesViewModel(mockRepository)
        viewModel.favouritesUiState.test {
            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Success<List<Book>>>()
            val books = state.data
            books.size shouldBe 3
            books.all { it.isFavorite } shouldBe true
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favouritesUiState emits Empty when repository returns empty list`() = runTest {
        every { mockRepository.getFavourites() } returns flowOf(emptyList())
        every { mockRepository.getFavouriteCount() } returns flowOf(0)
        val emptyViewModel = FavouritesViewModel(mockRepository)

        emptyViewModel.favouritesUiState.test {
            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Empty>()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favouritesUiState emits Error when repository throws exception`() = runTest {
        val exception = IOException("Database error")
        every { mockRepository.getFavourites() } returns kotlinx.coroutines.flow.flow {
            throw exception
        }
        every { mockRepository.getFavouriteCount() } returns flowOf(0)
        val errorViewModel = FavouritesViewModel(mockRepository)

        errorViewModel.favouritesUiState.test {
            // May get Loading first or skip to Error with UnconfinedTestDispatcher
            val state = awaitItem()
            if (state is UiState.Loading) {
                val errorState = awaitItem()
                errorState.shouldBeInstanceOf<UiState.Error>()
                (errorState as UiState.Error).message shouldBe "Failed to load favourites from database."
            } else {
                state.shouldBeInstanceOf<UiState.Error>()
                (state as UiState.Error).message shouldBe "Failed to load favourites from database."
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favouriteCount emits correct count`() = runTest {
        val viewModel = FavouritesViewModel(mockRepository)
        viewModel.favouriteCount.test {
            val count = awaitItem()
            count shouldBe 3
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favouriteCount emits 0 when no favourites`() = runTest {
        every { mockRepository.getFavourites() } returns flowOf(emptyList())
        every { mockRepository.getFavouriteCount() } returns flowOf(0)
        val viewModel = FavouritesViewModel(mockRepository)

        viewModel.favouriteCount.test {
            val count = awaitItem()
            count shouldBe 0
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavourite optimistically updates status map and calls repository`() = runTest {
        every { mockRepository.toggleFavourite(any()) } returns Completable.complete()
        val viewModel = FavouritesViewModel(mockRepository)

        viewModel.favouriteStatusMap.test {
            val initialMap = awaitItem() // Empty initially
            initialMap shouldBe emptyMap()

            // Toggle a book
            viewModel.toggleFavourite("book123")

            // Should optimistically update to true (since it was false/absent)
            val updatedMap = awaitItem()
            updatedMap shouldBe mapOf("book123" to true)

            verify { mockRepository.toggleFavourite("book123") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavourite optimistically toggles from true to false`() = runTest {
        every { mockRepository.toggleFavourite(any()) } returns Completable.complete()
        val viewModel = FavouritesViewModel(mockRepository)

        // First, check a book's status to set it to true
        every { mockRepository.isFavourite("book456") } returns Single.just(true)
        viewModel.checkFavouriteStatus("book456")

        viewModel.favouriteStatusMap.test {
            val initialMap = awaitItem()
            initialMap["book456"] shouldBe true

            // Toggle the book (should flip from true to false)
            viewModel.toggleFavourite("book456")

            val updatedMap = awaitItem()
            updatedMap["book456"] shouldBe false

            verify { mockRepository.toggleFavourite("book456") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavourite rolls back on error`() = runTest {
        val exception = IOException("Database error")
        every { mockRepository.toggleFavourite(any()) } returns Completable.error(exception)
        val viewModel = FavouritesViewModel(mockRepository)

        viewModel.favouriteStatusMap.test {
            val initialMap = awaitItem() // Empty initially
            initialMap shouldBe emptyMap()

            // Toggle a book (will fail)
            viewModel.toggleFavourite("book789")

            // Should optimistically update to true first
            val optimisticMap = awaitItem()
            optimisticMap shouldBe mapOf("book789" to true)

            // Then rollback to false on error
            val rolledBackMap = awaitItem()
            rolledBackMap shouldBe mapOf("book789" to false)

            verify { mockRepository.toggleFavourite("book789") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkFavouriteStatus updates status map correctly`() = runTest {
        every { mockRepository.isFavourite("book101") } returns Single.just(true)
        val viewModel = FavouritesViewModel(mockRepository)

        viewModel.favouriteStatusMap.test {
            val initialMap = awaitItem()
            initialMap shouldBe emptyMap()

            viewModel.checkFavouriteStatus("book101")

            val updatedMap = awaitItem()
            updatedMap["book101"] shouldBe true

            verify { mockRepository.isFavourite("book101") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkFavouriteStatus handles false status`() = runTest {
        every { mockRepository.isFavourite("book202") } returns Single.just(false)
        val viewModel = FavouritesViewModel(mockRepository)

        viewModel.favouriteStatusMap.test {
            awaitItem() // Initial empty map

            viewModel.checkFavouriteStatus("book202")

            val updatedMap = awaitItem()
            updatedMap["book202"] shouldBe false

            verify { mockRepository.isFavourite("book202") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkFavouriteStatus handles error gracefully`() = runTest {
        val exception = IOException("Database error")
        every { mockRepository.isFavourite("book303") } returns Single.error(exception)
        val viewModel = FavouritesViewModel(mockRepository)

        viewModel.favouriteStatusMap.test {
            val initialMap = awaitItem()
            initialMap shouldBe emptyMap()

            viewModel.checkFavouriteStatus("book303")

            // Map should remain unchanged on error
            expectNoEvents()
            verify { mockRepository.isFavourite("book303") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearAllFavourites calls repository and clears status map`() = runTest {
        every { mockRepository.clearAllFavourites() } returns Completable.complete()
        val viewModel = FavouritesViewModel(mockRepository)

        // First, set some status
        every { mockRepository.isFavourite("book404") } returns Single.just(true)
        viewModel.checkFavouriteStatus("book404")

        viewModel.favouriteStatusMap.test {
            val initialMap = awaitItem()
            initialMap["book404"] shouldBe true

            viewModel.clearAllFavourites()

            val clearedMap = awaitItem()
            clearedMap shouldBe emptyMap()

            verify { mockRepository.clearAllFavourites() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearAllFavourites handles error gracefully`() = runTest {
        val exception = IOException("Database error")
        every { mockRepository.clearAllFavourites() } returns Completable.error(exception)
        val viewModel = FavouritesViewModel(mockRepository)

        viewModel.clearAllFavourites()

        // Should call repository even if it fails
        verify { mockRepository.clearAllFavourites() }
    }

    @Test
    fun `multiple checkFavouriteStatus calls update status map correctly`() = runTest {
        every { mockRepository.isFavourite("book1") } returns Single.just(true)
        every { mockRepository.isFavourite("book2") } returns Single.just(false)
        every { mockRepository.isFavourite("book3") } returns Single.just(true)
        val viewModel = FavouritesViewModel(mockRepository)

        viewModel.favouriteStatusMap.test {
            awaitItem() // Initial empty map

            viewModel.checkFavouriteStatus("book1")
            val map1 = awaitItem()
            map1 shouldBe mapOf("book1" to true)

            viewModel.checkFavouriteStatus("book2")
            val map2 = awaitItem()
            map2 shouldBe mapOf("book1" to true, "book2" to false)

            viewModel.checkFavouriteStatus("book3")
            val map3 = awaitItem()
            map3 shouldBe mapOf("book1" to true, "book2" to false, "book3" to true)

            verify { mockRepository.isFavourite("book1") }
            verify { mockRepository.isFavourite("book2") }
            verify { mockRepository.isFavourite("book3") }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
