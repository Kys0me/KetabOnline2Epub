package off.kys.ketabonline2epub.presentation.screen.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import off.kys.ketabonline2epub.presentation.event.MainUiEvent
import off.kys.ketabonline2epub.presentation.state.MainUiState

@Composable
fun MainContent(
    state: MainUiState,
    onEvent: (MainUiEvent) -> Unit
) {
    val listState = rememberLazyListState()

    // Scroll to top when results change
    LaunchedEffect(key1 = state.searchResults) {
        listState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Error Handling Logic
        if (state.errorMessage != null) {
            ErrorScreen(
                message = state.errorMessage,
                onRetry = { onEvent(MainUiEvent.OnSearchClicked) }
            )
        } else if (state.searchResults.isEmpty()) {
            NoSearches()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                items(items = state.searchResults, key = { it.id.value }) { book ->
                    BookListItem(
                        book = book,
                        onDownloadClick = { onEvent(MainUiEvent.OnDownloadClicked(book)) }
                    )
                }
            }
        }
    }
}