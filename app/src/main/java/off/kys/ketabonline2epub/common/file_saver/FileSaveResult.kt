package off.kys.ketabonline2epub.common.file_saver

sealed class FileSaveResult {
    object Success : FileSaveResult()
    data class Error(val message: String) : FileSaveResult()
    object Cancelled : FileSaveResult()
}