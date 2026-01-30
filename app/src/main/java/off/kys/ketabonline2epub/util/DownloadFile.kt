package off.kys.ketabonline2epub.util

import android.util.Log
import off.kys.ketabonline2epub.util.extensions.toFile
import java.io.FileOutputStream
import java.net.URL

private const val TAG = "DownloadFile"

/**
 * Downloads a file from [url] to the specified [targetPath].
 * Uses standard Java/Kotlin libraries only.
 */
fun downloadFile(url: String, targetPath: String) {
    Log.d(TAG, "Starting download from: $url")
    
    try {
        URL(url).openStream().use { inputStream ->
            FileOutputStream(targetPath.toFile()).use { outputStream ->
                // This buffers the data in 8KB chunks
                val bytesCopied = inputStream.copyTo(outputStream)
                Log.d(TAG, "Download complete. Saved to: $targetPath")
                Log.d(TAG, "Total bytes downloaded: $bytesCopied")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to download file from $url", e)
        throw e
    }
}