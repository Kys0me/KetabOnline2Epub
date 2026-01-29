package off.kys.ketabonline2epub.save_file

sealed class FileSaveResult {
    object Success : FileSaveResult()
    data class Error(val message: String) : FileSaveResult()
    object Cancelled : FileSaveResult()
}