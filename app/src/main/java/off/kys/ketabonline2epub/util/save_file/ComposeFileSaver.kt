package off.kys.ketabonline2epub.util.save_file

import off.kys.ketabonline2epub.common.file_saver.DocumentType

interface ComposeFileSaver {
    fun save(fileName: String, type: DocumentType, data: ByteArray)
}