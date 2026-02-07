package off.kys.ketabonline2epub.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import off.kys.ketabonline2epub.common.BookType
import off.kys.ketabonline2epub.data.repository.BookDownloadTracker
import org.koin.compose.koinInject

/**
 * Remembers the state of downloaded books from a [BookDownloadTracker].
 *
 * @param tracker The [BookDownloadTracker] to observe.
 * @return A [State] object containing a map of downloaded book IDs to their [BookType].
 */
@Composable
fun rememberDownloadedBooks(tracker: BookDownloadTracker = koinInject()): State<Map<String, BookType>> =
    tracker.observeAllDownloads()
        .collectAsState(initial = emptyMap()) // We collect the flow into a Compose State