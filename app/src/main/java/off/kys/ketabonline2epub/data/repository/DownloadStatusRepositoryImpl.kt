package off.kys.ketabonline2epub.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import off.kys.ketabonline2epub.common.BookType
import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.repository.DownloadStatusRepository

class DownloadStatusRepositoryImpl(context: Context): DownloadStatusRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("book_downloads", Context.MODE_PRIVATE)

    // Helper to generate unique keys: "download_101_PDF"
    private fun getKey(bookId: String, type: BookType) = "download_${bookId}_${type.name}"

    override fun observeDownloadStatus(bookId: BookId, type: BookType): Flow<Boolean> = callbackFlow {
        val key = getKey(bookId.toString(), type)
        
        // 1. Emit initial value immediately
        trySend(prefs.getBoolean(key, false))

        // 2. Define the listener
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, changedKey ->
            if (changedKey == key) {
                trySend(sharedPrefs.getBoolean(key, false))
            }
        }

        // 3. Register listener
        prefs.registerOnSharedPreferenceChangeListener(listener)

        // 4. Cleanup when Flow collection stops (UI destroyed/scrolled away)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged() // Only emit if value actually changed

    override fun setDownloadStatus(bookType: BookType, bookId: BookId, isDownloaded: Boolean) {
        prefs.edit { putBoolean(getKey(bookId.toString(), bookType), isDownloaded) }
    }
}