package off.kys.ketabonline2epub.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import off.kys.github_app_updater.checkAppUpdate
import off.kys.github_app_updater.common.ChangelogSource
import off.kys.ketabonline2epub.BuildConfig
import off.kys.ketabonline2epub.R
import off.kys.ketabonline2epub.common.BookType
import off.kys.ketabonline2epub.common.Constants
import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.repository.BookRepository
import off.kys.ketabonline2epub.domain.repository.DownloadStatusRepository
import off.kys.ketabonline2epub.domain.repository.EpubConverterRepository
import off.kys.ketabonline2epub.presentation.event.MainUiEvent
import off.kys.ketabonline2epub.presentation.state.MainUiState
import off.kys.ketabonline2epub.util.downloadFile
import off.kys.ketabonline2epub.util.extensions.getFileName
import off.kys.ketabonline2epub.util.extensions.toURL
import off.kys.ketabonline2epub.util.processJsonToPdf
import off.kys.ketabonline2epub.util.unzip
import java.io.File

private const val TAG = "MainViewModel"

/**
 * The primary ViewModel for managing the book search and EPUB generation lifecycle.
 *
 * It bridges the UI layer with the [BookRepository] for data fetching and the
 * [EpubConverterRepository] for transforming raw book data into EPUB files.
 */
class MainViewModel(
    private val application: Application,
    private val bookRepository: BookRepository,
    private val epubConverterRepository: EpubConverterRepository,
    private val downloadStatus: DownloadStatusRepository
) : AndroidViewModel(application) {

    // Internal mutable state flow for UDF
    private val _uiState = MutableStateFlow(MainUiState())

    /** Public read-only state flow observed by the Compose UI. */
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        onEvent(MainUiEvent.CheckForUpdates)
    }

    /**
     * Unified entry point for UI interactions.
     * @param event The [MainUiEvent] representing user intent.
     */
    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.OnBookNameChange -> {
                _uiState.update {
                    it.copy(
                        searchQuery = event.name,
                        // If user clears the text, reset hasSearched so we show the Placeholder again
                        hasSearched = if (event.name.isBlank()) false else it.hasSearched,
                        searchResults = if (event.name.isBlank()) emptyList() else it.searchResults
                    )
                }
            }

            MainUiEvent.OnSearchClicked -> searchBooks()
            is MainUiEvent.OnDownloadClicked -> downloadBook(
                event.bookType,
                event.bookId
            )

            // Resets the downloaded file state after the UI has handled the file (e.g., showed the saver)
            MainUiEvent.DownloadHandled -> _uiState.update { it.copy(downloadedFile = null) }

            MainUiEvent.CheckForUpdates -> checkForUpdates()
            MainUiEvent.OnDismissUpdateDialog -> {
                _uiState.update { it.copy(isUpdateAvailable = false) }
            }

            is MainUiEvent.MarkAsDownloaded -> {
                downloadStatus.setDownloadStatus(bookType = event.bookType, bookId = event.bookId, isDownloaded = true)
                Log.e(TAG, "MarkAsDownloaded: ${event.bookType}, ${event.bookId}")
            }
        }
    }

    /**
     * Checks if a book has been downloaded.
     *
     * @param bookId The ID of the book.
     * @param bookType The type of the book (EPUB or PDF).
     *
     * @return A [Flow] emitting a [Boolean] indicating whether the book has been downloaded.
     */
    fun isBookDownloaded( bookId: BookId, bookType: BookType): Flow<Boolean> = downloadStatus.observeDownloadStatus(bookId, bookType)

    private fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                checkAppUpdate {
                    githubRepo(Constants.PROJECT_REPOSITORY_URL)
                    currentVersion(BuildConfig.VERSION_NAME)
                    changelogSource(ChangelogSource.RELEASE_BODY)
                    onUpdateAvailable { result ->
                        _uiState.update {
                            it.copy(
                                isUpdateAvailable = true,
                                newVersionName = result.latestVersion,
                                newVersionChangelog = result.changeLog,
                                updateUrl = result.downloadUrls.first().browserDownloadUrl
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed", e)
            }
        }
    }

    /**
     * Searches for books based on the current name in the UI state.
     * Performs a network call on the IO dispatcher.
     */
    private fun searchBooks() {
        val currentName = _uiState.value.searchQuery
        if (currentName.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    searchResults = emptyList(),
                    hasSearched = false // Reset this while a NEW search is in progress
                )
            }
            try {
                val results = bookRepository.searchBooks(currentName, 1)
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isLoading = false,
                        hasSearched = true // Set to true only after results (or empty list) come back
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Search failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Search failed: ${e.localizedMessage}",
                        hasSearched = true // Set to true even on error to stop the loading state
                    )
                }
            }
        }
    }

    private fun downloadBook(bookType: BookType, bookId: BookId) =
        when (bookType) {
            is BookType.EPUB -> downloadEpub(bookId)
            is BookType.PDF -> downloadPdf(bookId)
        }

    private fun downloadEpub(bookId: BookId) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Step 1 & 2: Data Acquisition
                val bookIndex = bookRepository.getBookIndex(bookId)
                val bookData = bookRepository.getBookData(bookId)

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
                        bookId = bookId,
                        isLoading = false,
                        downloadedFile = file,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Download failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun downloadPdf(bookId: BookId) {
        // Avoid launching multiple downloads for the same ID if already loading
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = withContext(Dispatchers.IO) {
                runCatching {
                    // 1. Fetch metadata
                    val bookData = bookRepository.getBookData(bookId)
                    val pdfUrl =
                        bookData.pdfUrl ?: throw IllegalArgumentException("PDF URL not found")

                    // 2. Prepare file locations
                    val pdfName = pdfUrl.toURL().getFileName() ?: "book_${bookId.value}"
                    val tempFile = File(application.cacheDir, "$pdfName.zip")

                    // 3. Download
                    downloadFile(pdfUrl, tempFile.absolutePath)

                    // 4. Processing (Unzip and Rename)
                    val unzippedFile =
                        unzip(tempFile) ?: throw IllegalStateException("Unzip failed")

                    val jsonToPdf = processJsonToPdf(
                        context = application,
                        jsonFile = unzippedFile
                    ) ?: throw IllegalStateException("JSON to PDF failed")

                    val finalFile = File(application.cacheDir, "${bookData.title}.pdf")

                    if (!jsonToPdf.renameTo(finalFile)) {
                        throw IllegalStateException("Failed to move file to final destination")
                    }

                    // Cleanup temp file
                    if (tempFile.exists()) tempFile.delete()
                    if (jsonToPdf.exists()) jsonToPdf.delete()

                    finalFile
                }
            }

            result.onSuccess { file ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        downloadedFile = file
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage
                    )
                }
            }
        }
    }
}