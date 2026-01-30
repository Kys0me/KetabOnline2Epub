package off.kys.ketabonline2epub

import java.io.File

// UI State representing the screen at any given moment
data class MainUiState(
    val bookId: BookId = BookId.Empty,
    val bookName: String = "",
    val searchResults: List<BookItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val downloadedFile: File? = null // For triggering the save dialog
)