package off.kys.ketabonline2epub

import android.content.Context
import java.io.File

fun Context.cacheDir(resolve: String): File =
    cacheDir.resolve(resolve)