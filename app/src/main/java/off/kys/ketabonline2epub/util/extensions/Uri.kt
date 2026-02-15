package off.kys.ketabonline2epub.util.extensions

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

fun Uri.getFileExtension(context: Context): String? = if (this.scheme == "content") {
    // If the URI is a content:// URI, we ask the ContentResolver for the MIME type
    val mime = MimeTypeMap.getSingleton()
    mime.getExtensionFromMimeType(context.contentResolver.getType(this))
} else {
    // If it's a file:// URI or something else, we pull the extension from the path
    MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(this.path ?: throw IllegalArgumentException("Invalid URI"))).toString())
}