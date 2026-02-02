package off.kys.ketabonline2epub.presentation.screen.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import off.kys.ketabonline2epub.presentation.event.MainUiEvent
import off.kys.ketabonline2epub.presentation.viewmodel.MainViewModel

@Composable
fun MainContent(
    viewModel: MainViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isDownloading = state.isLoading && state.searchResults.isNotEmpty()

    // Scroll to top when results change
    LaunchedEffect(key1 = state.searchResults) {
        listState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Error Handling Logic
        if (state.errorMessage != null) {
            ErrorScreen(
                message = state.errorMessage!!,
                onRetry = { viewModel.onEvent(MainUiEvent.OnSearchClicked) }
            )
        } else if (state.searchResults.isEmpty()) {
            NoSearches()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                items(items = state.searchResults, key = { it.id.value }) { book ->
                    val bookDownloaded by viewModel.isBookDownloaded(book.id).collectAsState(initial = false)

                    BookListItem(
                        book = book,
                        isDownloaded = bookDownloaded,
                        enabled = isDownloading.not()
                    ) {
                        viewModel.onEvent(MainUiEvent.OnDownloadClicked(book))
                    }
                }
            }
        }
    }
}