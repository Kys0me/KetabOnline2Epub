package off.kys.ketabonline2epub.util.extensions

import org.jsoup.Jsoup
import java.net.URI
import java.net.URLEncoder

fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8")

fun String.readUrlAsText(): String = URI(this).toURL().readText()

fun String.normalize(): String = this.replace("\n", "<br/>").replace("\r", "")

fun String.toPlainText(): String = Jsoup.parse(this).text()

fun String.escapeHtml(): String =
    replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")