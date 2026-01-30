package off.kys.ketabonline2epub.util.save_file

import off.kys.ketabonline2epub.common.file_saver.DocumentType

/**
 * Interface defining the contract for saving files within a Compose environment.
 * * This abstraction allows UI components to trigger a "Save As" flow without
 * needing to know the underlying Android implementation (like Activity Results
 * or ContentResolvers).
 */
interface ComposeFileSaver {

    /**
     * Triggers the file saving process.
     * * @param fileName The suggested name for the file (e.g., "book.epub").
     * @param type The [DocumentType] which determines the MIME type of the file.
     * @param data The raw byte content to be written to the file.
     */
    fun save(fileName: String, type: DocumentType, data: ByteArray)
}