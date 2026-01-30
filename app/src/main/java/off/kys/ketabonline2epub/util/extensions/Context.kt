package off.kys.ketabonline2epub.util.extensions

import android.content.Context
import java.io.File

fun Context.cacheDir(resolve: String): File =
    cacheDir.resolve(resolve)