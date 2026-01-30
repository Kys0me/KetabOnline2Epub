package off.kys.ketabonline2epub.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.ketabonline2epub.R
import off.kys.ketabonline2epub.common.logger
import off.kys.ketabonline2epub.domain.model.BookItem
import off.kys.ketabonline2epub.domain.repository.BookRepository
import off.kys.ketabonline2epub.domain.repository.EpubConverterRepository
import off.kys.ketabonline2epub.presentation.event.MainUiEvent
import off.kys.ketabonline2epub.presentation.state.MainUiState
import java.util.logging.Level

/**
 * The primary ViewModel for managing the book search and EPUB generation lifecycle.
 *
 * It bridges the UI layer with the [BookRepository] for data fetching and the
 * [EpubConverterRepository] for transforming raw book data into EPUB files.
 */
class MainViewModel(
    private val application: Application,
    private val bookRepository: BookRepository,
    private val epubConverterRepository: EpubConverterRepository
) : AndroidViewModel(application) {

    // Internal mutable state flow for UDF
    private val _uiState = MutableStateFlow(MainUiState())

    /** Public read-only state flow observed by the Compose UI. */
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /**
     * Unified entry point for UI interactions.
     * @param event The [MainUiEvent] representing user intent.
     */
    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.OnBookNameChange -> {
                _uiState.update { it.copy(bookName = event.name) }
            }

            MainUiEvent.OnSearchClicked -> searchBooks()
            is MainUiEvent.OnDownloadClicked -> downloadBook(event.book)

            // Resets the downloaded file state after the UI has handled the file (e.g., showed the saver)
            MainUiEvent.DownloadHandled -> _uiState.update { it.copy(downloadedFile = null) }
        }
    }

    /**
     * Searches for books based on the current name in the UI state.
     * Performs a network call on the IO dispatcher.
     */
    private fun searchBooks() {
        val currentName = _uiState.value.bookName

        if (currentName.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Fetch search results (hardcoded to page 1 for now)
                val results = bookRepository.searchBooks(currentName, 1)
                _uiState.update { it.copy(searchResults = results, isLoading = false) }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Search failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Search failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * orchestrates the multi-step process of converting a remote book to a local EPUB:
     * 1. Fetches the book index (Table of Contents).
     * 2. Fetches the book data (Actual pages/content).
     * 3. Builds chapters using the converter repository.
     * 4. Generates the final EPUB file.
     */
    private fun downloadBook(book: BookItem) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Step 1 & 2: Data Acquisition
                val bookIndex = bookRepository.getBookIndex(book.id)
                val bookData = bookRepository.getBookData(book.id)

                // Step 3: Transformation
                val chapters = epubConverterRepository.buildChapters(
                    indices = bookIndex,
                    pages = bookData.pages,
                    defaultTitle = application.getString(R.string.default_untitled)
                )

                // Step 4: Final Generation
                val file = epubConverterRepository.generate(bookData, chapters)

                _uiState.update {
                    it.copy(
                        bookId = book.id,
                        bookName = book.title,
                        isLoading = false,
                        downloadedFile = file,
                    )
                }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Download failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Download failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}