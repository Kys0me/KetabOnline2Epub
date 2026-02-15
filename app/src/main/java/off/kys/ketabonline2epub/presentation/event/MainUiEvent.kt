package off.kys.ketabonline2epub.presentation.event

import off.kys.ketabonline2epub.common.BookType

// UI Events representing user actions
sealed class MainUiEvent {
    data class OnBookNameChange(val name: String) : MainUiEvent()
    object OnSearchClicked : MainUiEvent()
    data class OnDownloadClicked(val bookType: BookType) : MainUiEvent()
    object DownloadHandled : MainUiEvent() // To reset the download state after saving
    object CheckForUpdates : MainUiEvent()
    object OnDismissUpdateDialog : MainUiEvent()
    data class MarkAsDownloaded(val bookType: BookType) : MainUiEvent()
}