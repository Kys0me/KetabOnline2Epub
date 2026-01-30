package off.kys.ketabonline2epub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
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
fun BookListItem(
    book: BookItem,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard( // Switch to ElevatedCard
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(CardDefaults.shape),
            leadingContent = {
                // Book Cover with rounded corners and proper scaling
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book.coverUrl ?: R.drawable.book_cover)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.book_cover),
                    modifier = Modifier
                        .width(64.dp) // Standard M3 list image width
                        .aspectRatio(2/3f) // Standard book ratio
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    // If no URL, apply the tint to the placeholder
                    colorFilter = if (book.coverUrl == null)
                        ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    else null
                )
            },
            headlineContent = {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column {
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Added a subtle badge for book format or status
                    AssistChip(
                        modifier = Modifier.padding(top = 4.dp),
                        onClick = { /* info */ },
                        label = { Text("EPUB", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.round_description_24),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            },
            trailingContent = {
                FilledTonalIconButton(
                    onClick = onDownloadClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_download_24),
                        contentDescription = stringResource(R.string.download),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}