package off.kys.ketabonline2epub.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import off.kys.ketabonline2epub.domain.model.BookId

class BookDownloadTracker(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("book_prefs", Context.MODE_PRIVATE)

    /**
     * Observes the download status of a specific book.
     * Uses callbackFlow to turn the Listener into a Stream of data.
     */
    fun isBookDownloadedFlow(bookId: BookId): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (key == bookId.toString()) {
                trySend(sharedPrefs.getBoolean(bookId.toString(), false))
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        
        // Emit the current value immediately when starting
        trySend(prefs.getBoolean(bookId.toString(), false))

        // Clean up the listener when the flow is closed (e.g., Composable leaves composition)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    /**
     * Sets the download status of a book.
     * Uses SharedPreferences to store the download status.
     *
     * @param bookId The ID of the book.
     * @param downloaded Whether the book has been downloaded.
     */
    fun setDownloaded(bookId: String, downloaded: Boolean) {
        prefs.edit { putBoolean(bookId, downloaded) }
    }
}