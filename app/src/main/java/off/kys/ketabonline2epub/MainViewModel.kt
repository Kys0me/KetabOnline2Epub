package off.kys.ketabonline2epub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.logging.Level

class MainViewModel(
    private val application: Application,
    private val bookRepository: BookRepository,
    private val epubConverterRepository: EpubConverterRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.OnBookNameChange -> {
                _uiState.update { it.copy(bookName = event.name) }
            }

            MainUiEvent.OnSearchClicked -> searchBooks()
            is MainUiEvent.OnDownloadClicked -> downloadBook(event.book)
            MainUiEvent.DownloadHandled -> _uiState.update { it.copy(downloadedFile = null) }
        }
    }

    private fun searchBooks() {
        val currentName = _uiState.value.bookName

        if (currentName.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
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

    private fun downloadBook(book: BookItem) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bookIndex = bookRepository.getBookIndex(book.id)
                val bookData = bookRepository.getBookData(book.id)
                val chapters = epubConverterRepository.buildChapters(
                    indices = bookIndex,
                    pages = bookData.pages,
                    defaultTitle = application.getString(R.string.default_untitled)
                )
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