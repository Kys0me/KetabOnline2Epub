package off.kys.ketabonline2epub.presentation.screen.main.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import off.kys.ketabonline2epub.domain.model.BookItem

@Composable
fun BookList(
    books: List<BookItem>,
    listState: LazyListState,
    downloadedBookIds: Map<String, Any?>, // Pass only the map for better performance
    isInteractionEnabled: Boolean,
    onDownloadClick: (BookItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState
    ) {
        items(
            items = books,
            key = { it.id.value }
        ) { book ->
            BookListItem(
                book = book,
                isDownloaded = downloadedBookIds.containsKey(book.id.toString()),
                enabled = isInteractionEnabled
            ) {
                onDownloadClick(book)
            }
        }

        // Only show the "End of results" if we actually have items in the list
        if (books.isNotEmpty()) {
            item {
                SearchEndFooter()
            }
        }
    }
}