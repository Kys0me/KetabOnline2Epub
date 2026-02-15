package off.kys.ketabonline2epub.util.extensions

import java.io.File

/**
 * Renames the file to [newName] within the same directory.
 * @param newName The new name (including extension).
 * @return The renamed File object, or null if renaming failed.
 */
fun File.rename(newName: String): File? {
    val newFile = File(this.parentFile, newName)
    return if (this.renameTo(newFile)) newFile else null
}