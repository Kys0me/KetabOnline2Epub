package off.kys.ketabonline2epub.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

/**
 * Reads text from a URL efficiently using a BufferedReader.
 * @param urlString The direct link to the text file.
 * @return The content of the file as a String.
 */
fun readTextFromUrl(urlString: String): String {
    return URL(urlString).openConnection().apply {
        // Set a timeout so the app doesn't hang forever
        connectTimeout = 5000
        readTimeout = 5000
    }.getInputStream().use { inputStream ->
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }
}