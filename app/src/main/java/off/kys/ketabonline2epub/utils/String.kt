package off.kys.ketabonline2epub.utils

import java.net.URI
import java.net.URLEncoder

fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8")

fun String.readUrlAsText(): String = URI(this).toURL().readText()