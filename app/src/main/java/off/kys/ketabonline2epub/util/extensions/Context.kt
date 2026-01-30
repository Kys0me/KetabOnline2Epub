package off.kys.ketabonline2epub.util.extensions

import android.content.Context
import java.io.File

/**
 * Resolves a child file or directory within the application's internal cache directory.
 * * This is useful for creating temporary paths for scraping or EPUB assembly
 * without manually concatenating path strings.
 *
 * @param resolve The relative path or filename to resolve against the cache directory.
 * @return A [File] object pointing to the resolved path within [Context.getCacheDir].
 */
fun Context.cacheDir(resolve: String): File =
    cacheDir.resolve(resolve)