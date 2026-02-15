package off.kys.ketabonline2epub.presentation.screen.main.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import off.kys.ketabonline2epub.R
import off.kys.ketabonline2epub.presentation.event.MainUiEvent
import off.kys.ketabonline2epub.presentation.screen.components.ErrorScreen
import off.kys.ketabonline2epub.presentation.screen.components.LoadingScreen
import off.kys.ketabonline2epub.presentation.utils.rememberDownloadedBooks
import off.kys.ketabonline2epub.presentation.viewmodel.MainViewModel

@Composable
fun MainContent(
    viewModel: MainViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val downloadTracker by rememberDownloadedBooks()

    // --- State Logic Helpers ---
    val errorMessage = state.errorMessage
    val isError = errorMessage != null
    val isEmptyQuery = state.searchQuery.isBlank() && !state.hasSearched
    val isInitialLoading = state.isLoading && state.searchResults.isEmpty()
    val isLoadingResults = state.isLoading && state.searchResults.isNotEmpty()
    val noResultsFound = state.hasSearched && !state.isLoading && state.searchResults.isEmpty()

    // Automatically scroll to top when new search results arrive
    LaunchedEffect(key1 = state.searchResults) {
        if (state.searchResults.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // 1. Error State
            isError -> ErrorScreen(
                message = errorMessage,
                onRetry = { viewModel.onEvent(MainUiEvent.OnSearchClicked) }
            )

            // 2. Idle State (No query yet)
            isEmptyQuery -> SearchPlaceholderScreen()

            // 3. First-time Loading State
            isInitialLoading -> LoadingScreen(message = stringResource(R.string.searching_for_books))

            // 4. No Results Found State
            noResultsFound -> NoSearches() // Or a "No results found for..." screen

            // 5. Success State (List of Books)
            else -> BookList(
                books = state.searchResults,
                listState = listState,
                downloadedBookIds = downloadTracker,
                isInteractionEnabled = !isLoadingResults,
                onDownloadClick = { type ->
                    viewModel.onEvent(MainUiEvent.OnDownloadClicked(type))
                }
            )
        }
    }
}