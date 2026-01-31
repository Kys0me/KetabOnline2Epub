package off.kys.ketabonline2epub.presentation.screen.main.components

import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import off.kys.ketabonline2epub.R

@Composable
fun NewUpdateDialog(
    versionName: String,
    changelog: String,
    updateUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.new_update_available_ver, versionName))
        },
        text = {
            Text(text = changelog)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Open in browser
                    val intent = Intent(Intent.ACTION_VIEW, updateUrl.toUri())
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.update_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.later))
            }
        }
    )
}