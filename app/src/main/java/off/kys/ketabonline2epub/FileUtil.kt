package off.kys.ketabonline2epub

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.logging.Level
import java.util.zip.ZipInputStream

fun unzip(
    zipFile: File,
    targetDir: File = zipFile.parentFile!!,
    deleteWhenFinish: Boolean = true,
): Int {
    if (!zipFile.exists()) {
        logger.warning("Unzip failed: File ${zipFile.absolutePath} does not exist.")
        return -1
    }

    return try {
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
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (deleteWhenFinish) {
            zipFile.delete()
            logger.info("Deleted temp zip file: ${zipFile.absolutePath}")
        }
        0 // Success
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Error unzipping file", e)
        1 // Error
    }
}