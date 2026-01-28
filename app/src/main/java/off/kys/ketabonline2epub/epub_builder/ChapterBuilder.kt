package off.kys.ketabonline2epub.epub_builder

import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.service.MediatypeService
import java.nio.charset.StandardCharsets

@EpubDsl
class ChapterBuilder(
    private val title: String,
) {
    private val body = StringBuilder()

    operator fun String.unaryPlus() {
        body.append(this)
    }

    fun br() {
        body.append("<br>")
    }

    fun p(text: String) {
        body.append("<p>").append(text.escapeHtml()).append("</p>")
    }

    fun p(text: StringBuilder.() -> Unit) {
        body.append("<p>")
        val sb = StringBuilder()
        sb.text()
        body.append(sb)
        body.append("</p>")
    }

    fun h1(text: String) {
        body.append("<h1>").append(text.escapeHtml()).append("</h1>")
    }

    fun h2(text: String) {
        body.append("<h2>").append(text.escapeHtml()).append("</h2>")
    }

    fun img(src: String, alt: String = "") {
        body.append("<img src=\"").append(src.escapeHtml()).append("\" alt=\"").append(alt.escapeHtml()).append("\"/>")
    }

    fun raw(html: String) {
        body.append(html)
    }

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