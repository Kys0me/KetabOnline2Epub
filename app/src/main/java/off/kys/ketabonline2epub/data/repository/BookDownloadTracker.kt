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

class BookDownloadTracker(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.BOOK_DOWNLOAD_TRACKER_PREFS, Context.MODE_PRIVATE)

    /**
     * Stores a book. Key is the ID, Value is the type.
     */
    fun saveBook(bookId: BookId, type: BookType) {
        prefs.edit { putString(bookId.toString(), type.name) }
    }

    /**
     * Removes a book. Key is the ID.
     */
    fun removeBook(bookId: BookId) {
        prefs.edit { remove(bookId.toString()) }
    }

    fun getBookType(bookId: BookId): BookType {
        val typeString = prefs.getString(bookId.toString(), null) ?: throw IllegalStateException("Book not found")
        return BookType.valueOf(typeString)
    }

    fun isDownloaded(bookId: BookId): Boolean = prefs.contains(bookId.toString())

    /**
     * A Flow that emits a Map of all downloaded Book IDs and their types.
     * Updates automatically whenever SharedPreferences changes.
     */
    fun observeAllDownloads(): Flow<Map<String, BookType>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, _ ->
            val allEntries = p.all.mapValues { BookType.valueOf(it.value as String) }
            trySend(allEntries)
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        // Initial value
        val initialValue = prefs.all.mapValues { BookType.valueOf(it.value as String) }
        trySend(initialValue)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}