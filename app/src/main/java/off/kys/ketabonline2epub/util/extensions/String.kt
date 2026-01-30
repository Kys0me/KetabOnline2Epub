package off.kys.ketabonline2epub.util.extensions

import org.jsoup.Jsoup
import java.io.File
import java.net.URI
import java.net.URLEncoder

/**
 * URL-encodes a string using the UTF-8 character set.
 * Useful for preparing query parameters or path segments.
 */
fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8")

/**
 * Fetches the content from the given URL string and returns it as a plain string.
 * Note: This performs a blocking network operation.
 */
fun String.readUrlAsText(): String = URI(this).toURL().readText()

/**
 * Normalizes line endings for HTML rendering.
 * Replaces newlines with `<br/>` tags and removes carriage returns.
 */
fun String.normalize(): String = this.replace("\n", "<br/>").replace("\r", "")

/**
 * Strips all HTML tags from a string and returns the raw text content.
 * Powered by Jsoup for robust parsing.
 */
fun String.toPlainText(): String = Jsoup.parse(this).text()

/**
 * Replaces special characters with their corresponding HTML entities.
 * Prevents XSS or malformed HTML when inserting raw text into tags.
 */
fun String.escapeHtml(): String =
    replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

/**
 * Converts a string to a File object.
 * Useful for handling file paths or URLs.
 */
fun String.toFile() = File(this)
