@file:Suppress("unused")

package off.kys.ketabonline2epub.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import off.kys.ketabonline2epub.common.BookType
import off.kys.ketabonline2epub.common.Constants
import off.kys.ketabonline2epub.domain.model.BookId

// TODO: This tracker is broken, write another one that is more compatible with the current structure.
class BookDownloadTracker(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.BOOK_DOWNLOAD_TRACKER_PREFS, Context.MODE_PRIVATE)

    /**
     * Stores a book. Key is the ID, Value is the type.
     */
    fun saveBook(book: BookType) {
        val key = book.getKey()
        prefs.edit { putString(key, book.name) }
    }

    /**
     * Removes a book. Key is the ID.
     */
    fun removeBook(book: BookType) {
        prefs.edit { remove(book.getKey()) }
    }

    /**
     * Checks if a book is downloaded. Key is the ID.
     *
     * @param book The book to check.
     *
     * @return True if the book is downloaded, false otherwise.
     */
    fun isDownloaded(book: BookType): Boolean =
        prefs.contains(book.getKey())

    /**
     * A Flow that emits a Map of all downloaded Book IDs and their types.
     * Updates automatically whenever SharedPreferences changes.
     */
    fun observeAllDownloads(): Flow<Map<String, BookType>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, _ ->
            val allEntries = p.all.mapValues { (id, typeName) ->
                // id is the key (BookId string), typeName is the value ("PDF"/"EPUB")
                BookType.valueOf(typeName as String, BookId(id.toIntOrNull() ?: throw IllegalStateException("Invalid ID")))
            }
            trySend(allEntries)
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        // Initial value
        val initialValue = prefs.all.mapValues { (id, typeName) ->
            BookType.valueOf(typeName as String, BookId(id.toIntOrNull() ?: throw IllegalStateException("Invalid ID")))
        }
        trySend(initialValue)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun BookType.getKey() = "${bookId}_${name}"
}