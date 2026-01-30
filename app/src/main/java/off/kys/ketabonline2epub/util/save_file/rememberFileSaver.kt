package off.kys.ketabonline2epub.util.save_file

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import off.kys.ketabonline2epub.common.file_saver.DocumentType
import off.kys.ketabonline2epub.common.file_saver.FileSaveResult

@Suppress("AssignedValueIsNeverRead")
@Composable
fun rememberFileSaver(
    onResult: (FileSaveResult) -> Unit = {}
): ComposeFileSaver {
    val context = LocalContext.current
    var pendingData by remember { mutableStateOf<ByteArray?>(null) }
    var currentMimeType by remember { mutableStateOf(DocumentType.BINARY.mimeType) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(currentMimeType)
    ) { uri: Uri? ->
        if (uri == null) {
            onResult(FileSaveResult.Cancelled)
            return@rememberLauncherForActivityResult
        }

        try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(pendingData ?: byteArrayOf())
                onResult(FileSaveResult.Success)
            } ?: throw Exception("Could not open output stream")
        } catch (e: Exception) {
            onResult(FileSaveResult.Error(e.localizedMessage ?: "Unknown Error"))
        } finally {
            pendingData = null
        }
    }

    return remember(launcher) {
        object : ComposeFileSaver {
            override fun save(fileName: String, type: DocumentType, data: ByteArray) {
                pendingData = data
                currentMimeType = type.mimeType
                launcher.launch(fileName)
            }
        }
    }
}