package off.kys.ketabonline2epub.presentation.screen.main.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import off.kys.ketabonline2epub.common.BookType
import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.model.BookItem
import off.kys.ketabonline2epub.presentation.viewmodel.MainViewModel

/**
 * A composable that displays a list of books.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param books The list of books to display.
 * @param listState The state of the lazy list.
 * @param viewModel The main view model.
 * @param isInteractionEnabled A flag indicating whether interaction is enabled.
 * @param onDownloadClick A callback that is invoked when a download button is clicked.
 */
@Composable
fun BookList(
    modifier: Modifier = Modifier,
    books: List<BookItem>,
    listState: LazyListState,
    viewModel: MainViewModel,
    isInteractionEnabled: Boolean,
    onDownloadClick: (BookType, BookId) -> Unit
) {
    when {
        books.isEmpty() -> {
            EmptySearchPlaceholder()
        }
        else -> {
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
                        isDownloaded = viewModel::isBookDownloaded,
                        enabled = isInteractionEnabled,
                        onDownloadClick = { onDownloadClick(it, book.id) }
                    )
                }

                // Only show the "End of results" if we actually have items in the list
                if (books.isNotEmpty()) {
                    item {
                        SearchEndFooter()
                    }
                }
            }
        }
    }
}