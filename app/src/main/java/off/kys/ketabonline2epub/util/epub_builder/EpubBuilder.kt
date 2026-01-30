@file:Suppress("unused")

package off.kys.ketabonline2epub.util.epub_builder

import android.util.Base64
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.MediaType
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.service.MediatypeService
import off.kys.ketabonline2epub.domain.model.Base64Image
import off.kys.ketabonline2epub.util.epub_builder.dsl.EpubDsl
import java.nio.charset.StandardCharsets

@EpubDsl
class EpubBuilder(
    private val title: String
) {
    private val book = Book().apply {
        metadata.addTitle(this@EpubBuilder.title)
    }

    val bookCover: Resource?
        get() = book.coverImage

    fun author(name: String) {
        book.metadata.addAuthor(Author(name))
    }

    fun language(lang: String) {
        book.metadata.language = lang
    }

    fun css(content: String) {
        val resource = Resource(
            content.toByteArray(StandardCharsets.UTF_8),
            MediatypeService.CSS
        )
        book.resources.add(resource)
    }

    fun cover(
        base64: Base64Image,
        mediaType: MediaType? = null // optional, will try to detect if null
    ) {
        if (base64.isEmpty)
            return
        // Strip Data URI prefix if present
        val cleanBase64 = base64.value!!.substringAfter("base64,", base64.value)

        // Decode
        val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)

        // Detect media type if not provided
        val type = mediaType ?: when {
            cleanBase64.startsWith("iVBOR") -> MediatypeService.PNG
            cleanBase64.startsWith("/9j/") -> MediatypeService.JPG
            else -> MediatypeService.PNG // fallback
        }

        book.coverImage = Resource(bytes, type)
    }

    fun chapter(
        title: String,
        block: ChapterBuilder.() -> Unit
    ) {
        val builder = ChapterBuilder(title).apply(block)
        book.addSection(title, builder.build())
    }

    fun image(
        bytes: ByteArray,
        mediaType: MediaType = MediatypeService.PNG
    ) {
        book.resources.add(Resource(bytes, mediaType))
    }

    fun build(): Book = book
}