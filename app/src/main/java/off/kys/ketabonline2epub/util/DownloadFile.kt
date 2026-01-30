package off.kys.ketabonline2epub.util

import off.kys.ketabonline2epub.common.logger
import off.kys.ketabonline2epub.util.extensions.toFile
import java.io.FileOutputStream
import java.net.URL
import java.util.logging.Level

/**
 * Downloads a file from [url] to the specified [targetPath].
 * Uses standard Java/Kotlin libraries only.
 */
fun downloadFile(url: String, targetPath: String) {
    logger.info("Starting download from: $url")
    
    try {
        URL(url).openStream().use { inputStream ->
            FileOutputStream(targetPath.toFile()).use { outputStream ->
                // This buffers the data in 8KB chunks
                val bytesCopied = inputStream.copyTo(outputStream)
                logger.info("Download complete. Saved to: $targetPath")
                logger.fine("Total bytes downloaded: $bytesCopied")
            }
        }
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Failed to download file from $url", e)
        throw e
    }
}