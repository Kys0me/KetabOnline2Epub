package off.kys.ketabonline2epub.presentation.state

import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.model.BookItem
import off.kys.ketabonline2epub.util.extensions.Empty
import java.io.File

// UI State representing the screen at any given moment
data class MainUiState(
    val bookId: BookId = BookId.Empty,
    val bookName: String = "",
    val searchResults: List<BookItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val downloadedFile: File? = null, // For triggering the save dialog
    val isUpdateAvailable: Boolean = false,
    val newVersionName: String = String.Empty(),
    val newVersionChangelog: String = String.Empty(),
    val updateUrl: String = String.Empty()
)