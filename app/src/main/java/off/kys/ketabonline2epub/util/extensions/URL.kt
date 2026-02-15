package off.kys.ketabonline2epub.util.extensions

import android.util.Log
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private const val TAG = "URL_EXT"

/**
 * Extracts the filename from a URL, handling path segments,
 * URL encoding, and stripping query parameters/fragments.
 */
fun URL.getFileName(): String? = try {
    // 1. Use toURI() to properly separate the path from queries/fragments
    val uri = this.toURI()
    val path = uri.path // Returns the decoded path without ?query or #fragment

    if (path.isNullOrBlank()) {
        null
    } else {
        // 2. Extract the last segment
        val name = path.substringAfterLast('/')

        // 3. Double-check if the result is empty (e.g., trailing slash "example.com/dir/")
        if (name.isNotEmpty()) {
            // StandardCharsets is safer than passing a string literal "UTF-8"
            URLDecoder.decode(name, StandardCharsets.UTF_8.name())
        } else {
            null
        }
    }
} catch (e: Exception) {
    Log.e(TAG, "Failed to extract filename from URL: $this", e)
    null
}