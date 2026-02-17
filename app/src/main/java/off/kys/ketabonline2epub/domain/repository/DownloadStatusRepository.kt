package off.kys.ketabonline2epub.domain.repository

import kotlinx.coroutines.flow.Flow
import off.kys.ketabonline2epub.common.BookType
import off.kys.ketabonline2epub.domain.model.BookId

interface DownloadStatusRepository {
    /**
     * A Flow that watches SharedPreferences.
     * Whenever the specific key changes, it emits the new boolean value.
     *
     * @param bookId id of book
     * @param type type of book
     * @return flow of download status
     */
    fun observeDownloadStatus(bookId: BookId, type: BookType): Flow<Boolean>

    /**
     * Set the download status of a book.
     *
     * @param bookType book to set download status for
     * @param bookId id of book
     * @param isDownloaded download status
     */
    fun setDownloadStatus(bookType: BookType, bookId: BookId, isDownloaded: Boolean)
}