package off.kys.ketabonline2epub.util

import off.kys.ketabonline2epub.common.logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.logging.Level
import java.util.zip.ZipInputStream

/**
 * Extracts a ZIP archive to a specified directory.
 *
 * @param zipFile The source ZIP file to be extracted.
 * @param targetDir The directory where files will be extracted. Defaults to the same directory as the source.
 * @param deleteWhenFinish If true, the source ZIP file will be deleted after successful extraction.
 * @return The last file extracted from the archive, or null if the extraction failed or the ZIP was empty.
 */
fun unzip(
    zipFile: File,
    targetDir: File = zipFile.parentFile!!,
    deleteWhenFinish: Boolean = true,
): File? {
    if (!zipFile.exists()) {
        logger.warning("Unzip failed: File ${zipFile.absolutePath} does not exist.")
        return null
    }

    var extractedFile: File? = null

    try {
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDir, entry.name)

                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    // Ensure parent directories exist for the nested file
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    // Capture the reference to the file (useful for single-file zips)
                    extractedFile = newFile
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (deleteWhenFinish) {
            zipFile.delete()
        }

        return extractedFile
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Error unzipping file", e)
        return null
    }
}