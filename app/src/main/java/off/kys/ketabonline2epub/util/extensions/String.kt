package off.kys.ketabonline2epub.util.extensions

import org.jsoup.Jsoup
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
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
 * Normalizes text for XHTML rendering by replacing various newline formats
 * with `<br/>` tags and removing standalone carriage returns.
 */
fun String.normalize(): String = this
    .replace("\r\n", "<br/>") // Handle Windows line endings
    .replace("\n", "<br/>")   // Handle Unix line endings
    .replace("\r", "")        // Clean up remaining carriage returns

/**
 * Converts an HTML string to plain text, preserving basic structural spacing
 * while enforcing a maximum limit on consecutive newlines.
 *
 * @param maxNewLines The maximum number of consecutive empty lines allowed between blocks.
 * Defaults to 2 (standard paragraph spacing).
 * @return A trimmed plain-text representation of the HTML.
 */
fun String.toPlainText(maxNewLines: Int = 2): String {
    val document = Jsoup.parse(this)
    val sb = StringBuilder()

    // Helper to append newlines without exceeding the limit
    fun appendNewLines(count: Int) {
        val currentEndingNewLines = sb.reversed().takeWhile { it == '\n' }.length
        val remainingToFill = count - currentEndingNewLines
        repeat(remainingToFill.coerceAtLeast(0)) { sb.append("\n") }
    }

    NodeTraversor.traverse(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {
            when {
                node is TextNode -> sb.append(node.text())
                node.nodeName() == "br" -> appendNewLines(1)
                node.nodeName() in listOf("p", "div", "h1", "h2", "h3", "li") && sb.isNotEmpty() -> {
                    appendNewLines(1)
                }
            }
        }

        override fun tail(node: Node, depth: Int) {
            if (node.nodeName() in listOf("p", "div", "h1", "h2", "h3", "li")) {
                // Ensure block elements are followed by the desired spacing
                appendNewLines(maxNewLines)
            }
        }
    }, document.body())

    return sb.toString().trim()
}

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

@Suppress("FunctionName", "NOTHING_TO_INLINE")
inline fun String.Companion.Empty() = ""