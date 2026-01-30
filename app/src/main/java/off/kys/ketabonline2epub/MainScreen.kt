package off.kys.ketabonline2epub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
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

@Composable
fun BookListItem(book: BookItem, onDownloadClick: () -> Unit) {
    val context = LocalContext.current

    ListItem(
        leadingContent = {
            val bookCover =
                ImageRequest.Builder(context)
                    .data(book.coverUrl?.ifNull { R.drawable.book_cover })
                    .build()

            AsyncImage(
                modifier = Modifier.size(
                    width = 100.dp,
                    height = 150.dp
                ),
                model = bookCover,
                contentDescription = stringResource(R.string.book_cover),
                colorFilter = ColorFilter.tint(color = if (book.coverUrl == null) MaterialTheme.colorScheme.onSurfaceVariant else Color.Unspecified)
            )
        },
        headlineContent = {
            Text(text = book.title)
        },
        supportingContent = {
            Text(text = book.author)
        },
        trailingContent = {
            TextButton(onClick = { onDownloadClick() }) {
                Text(stringResource(R.string.download))
            }
        }
    )
}