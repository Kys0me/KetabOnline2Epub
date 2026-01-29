package off.kys.ketabonline2epub.save_file

interface ComposeFileSaver {
    fun save(fileName: String, type: DocumentType, data: ByteArray)
}