@file:Suppress("unused")

package off.kys.ketabonline2epub.util.epub_builder

import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.service.MediatypeService
import off.kys.ketabonline2epub.util.epub_builder.dsl.EpubDsl
import off.kys.ketabonline2epub.util.extensions.escapeHtml
import off.kys.ketabonline2epub.util.extensions.normalize
import java.nio.charset.StandardCharsets

/**
 * A DSL builder for creating EPUB chapters with built-in RTL (Right-to-Left) support.
 * * This class leverages a [StringBuilder] to assemble XHTML content that is
 * compatible with EPUB 3 standards.
 * * @param title The title of the chapter, used in the HTML head and navigation.
 */
@EpubDsl
class ChapterBuilder(
    private val title: String,
) {
    private val body = StringBuilder()

    /**
     * Process text: Escape HTML special characters, then convert newlines to <br/>.
     */
    private fun String.prepare(): String = this.escapeHtml().normalize()

    /**
     * Appends text to the chapter body. Automatically handles escaping and newlines.
     */
    operator fun String.unaryPlus() {
        body.append(this.prepare())
    }

    /**
     * Appends a line break tag.
     */
    fun br() {
        body.append("<br/>")
    }

    /**
     * Wraps the text in a paragraph tag with normalized line breaks.
     */
    fun p(text: String) {
        body.append("<p>").append(text.prepare()).append("</p>")
    }

    /**
     * Higher-order function to build a paragraph block using a nested DSL.
     */
    fun p(block: StringBuilder.() -> Unit) {
        val sb = StringBuilder()
        sb.block()
        // We normalize the result of the block
        body.append("<p>").append(sb.toString().normalize()).append("</p>")
    }

    /**
     * Wraps [text] in an H1 tag.
     */
    fun h1(text: String) {
        body.append("<h1>").append(text.prepare()).append("</h1>")
    }

    /**
     * Wraps [text] in an H2 tag.
     */
    fun h2(text: String) {
        body.append("<h2>").append(text.prepare()).append("</h2>")
    }

    /**
     * Inserts an image tag. Note: src and alt are escaped but not normalized (newlines in URLs are invalid).
     */
    fun img(src: String, alt: String = "") {
        body.append("<img src=\"")
            .append(src.escapeHtml())
            .append("\" alt=\"")
            .append(alt.escapeHtml())
            .append("\"/>")
    }

    /**
     * Appends raw HTML string directly. Use with caution!
     */
    fun raw(html: String) {
        body.append(html)
    }

    /**
     * Compiles the builder content into an epublib [Resource].
     * * Includes a default CSS reset specifically designed for Arabic (RTL) typography,
     * ensuring proper text alignment and bidi (bidirectional) behavior.
     * * @param language The ISO language code (defaults to "ar").
     * @return A [Resource] containing the formatted XHTML content.
     */
    internal fun build(language: String = "ar"): Resource {
        val html = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE html>
        <html xmlns="http://www.w3.org/1999/xhtml"
              xmlns:epub="http://www.idpf.org/2007/ops"
              xml:lang="$language"
              lang="$language"
              dir="rtl">
          <head>
            <title>${title.escapeHtml()}</title>
            <meta charset="UTF-8"/>
            <style>
              body {
                direction: rtl;
                text-align: right;
                unicode-bidi: isolate;
              }

              section[epub|type~="chapter"] {
                direction: rtl;
                text-align: right;
              }

              p, h1, h2 {
                direction: rtl;
                text-align: right;
                unicode-bidi: plaintext;
              }

              /* Automatic LTR islands inside RTL text */
              .ltr {
                direction: ltr;
                unicode-bidi: embed;
                text-align: left;
              }

              /* Numbers, URLs, Latin words */
              span[dir="ltr"] {
                direction: ltr;
                unicode-bidi: embed;
              }

              img {
                max-width: 100%;
              }
            </style>
          </head>
          <body>
            <section epub:type="chapter">
              $body
            </section>
          </body>
        </html>
    """.trimIndent()

        return Resource(
            html.toByteArray(StandardCharsets.UTF_8),
            MediatypeService.XHTML
        )
    }
}