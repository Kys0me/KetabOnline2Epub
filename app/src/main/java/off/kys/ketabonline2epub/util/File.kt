package off.kys.ketabonline2epub.util

import off.kys.ketabonline2epub.common.logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.logging.Level
import java.util.zip.ZipInputStream

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
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    // Capture the reference to the file
                    extractedFile = newFile
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (deleteWhenFinish) {
            zipFile.delete()
        }

        return extractedFile // Returns the file (or null if zip was empty)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Error unzipping file", e)
        return null
    }
}