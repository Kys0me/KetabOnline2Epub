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

/**
 * A Composable utility that provides a mechanism to save files using the
 * Android Storage Access Framework (SAF).
 *
 * It manages the lifecycle of the [androidx.activity.result.ActivityResultLauncher] and maintains
 * the byte data temporarily while the user selects a destination.
 *
 * @param onResult Callback invoked when the save operation completes, is canceled, or fails.
 * @return An implementation of [ComposeFileSaver] to trigger the save dialog.
 */
@Composable
fun rememberFileSaver(
    onResult: (FileSaveResult) -> Unit = {}
): ComposeFileSaver {
    val context = LocalContext.current

    // Holds the data to be written once the user picks a URI
    var pendingData by remember { mutableStateOf<ByteArray?>(null) }

    // Tracks the MIME type for the CreateDocument intent
    val currentMimeType = remember { mutableStateOf(DocumentType.BINARY.mimeType) }

    // Registers the system dialog launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(currentMimeType.value)
    ) { uri: Uri? ->
        if (uri == null) {
            onResult(FileSaveResult.Cancelled)
            return@rememberLauncherForActivityResult
        }

        try {
            // Write the pending data to the selected URI via ContentResolver
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(pendingData ?: byteArrayOf())
                onResult(FileSaveResult.Success)
            } ?: throw Exception("Could not open output stream")
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(FileSaveResult.Error(e.localizedMessage ?: "Unknown Error"))
        } finally {
            // Clear the data from memory after the operation finishes
            pendingData = null
        }
    }

    // Memoize the saver object to prevent unnecessary recompositions
    return remember(launcher) {
        object : ComposeFileSaver {
            override fun save(fileName: String, type: DocumentType, data: ByteArray) {
                pendingData = data
                currentMimeType.value = type.mimeType
                launcher.launch(fileName)
            }
        }
    }
}