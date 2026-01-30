package off.kys.ketabonline2epub

// UI Events representing user actions
sealed class MainUiEvent {
    data class OnBookNameChange(val name: String) : MainUiEvent()
    object OnSearchClicked : MainUiEvent()
    data class OnDownloadClicked(val book: BookItem) : MainUiEvent()
    object DownloadHandled : MainUiEvent() // To reset the download state after saving
}