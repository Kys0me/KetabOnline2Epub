package off.kys.ketabonline2epub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import off.kys.ketabonline2epub.save_file.DocumentType
import off.kys.ketabonline2epub.save_file.rememberFileSaver
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val saveBook = rememberFileSaver()

    // Handle File Saving Side Effect
    LaunchedEffect(key1 = state.downloadedFile) {
        state.downloadedFile?.let { file ->
            saveBook.save(
                fileName = "${state.bookId.value} - ${file.name}",
                type = DocumentType.ALL,
                data = file.readBytes()
            )
            viewModel.onEvent(MainUiEvent.DownloadHandled)
        }
    }

    Scaffold(
        topBar = {
            BookSearchToolbar(
                mainUiState = state,
                onEvent = viewModel::onEvent
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Loading Indicator
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Results List
            LazyColumn {
                items(items = state.searchResults, key = { it.id.value }) { book ->
                    BookListItem(
                        book = book,
                        onDownloadClick = { viewModel.onEvent(MainUiEvent.OnDownloadClicked(book)) }
                    )
                }
            }
        }
    }
}