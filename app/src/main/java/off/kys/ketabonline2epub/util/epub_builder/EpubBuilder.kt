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

/**
 * The top-level DSL builder for generating an EPUB document.
 * * This class coordinates the assembly of book metadata, styling, covers, and chapters
 * into a final [Book] object compatible with the epublib framework.
 *
 * @param title The primary title of the book.
 */
@EpubDsl
class EpubBuilder(
    private val title: String
) {
    private val book = Book().apply {
        metadata.addTitle(this@EpubBuilder.title)
    }

    /** Accesses the current cover image resource of the book. */
    val bookCover: Resource?
        get() = book.coverImage

    /** Adds an author to the book's metadata. */
    fun author(name: String) {
        book.metadata.addAuthor(Author(name))
    }

    /** Sets the primary language of the book (e.g., "ar", "en"). */
    fun language(lang: String) {
        book.metadata.language = lang
    }

    /** Adds a global CSS stylesheet to the book's resources. */
    fun css(content: String) {
        val resource = Resource(
            content.toByteArray(StandardCharsets.UTF_8),
            MediatypeService.CSS
        )
        book.resources.add(resource)
    }

    /**
     * Sets the book cover using a Base64 encoded string.
     * * It automatically handles:
     * 1. Stripping Data URI prefixes (e.g., "data:image/png;base64,").
     * 2. Detecting the [MediaType] based on standard Base64 magic numbers if not provided.
     *
     * @param base64 The image data as a [Base64Image].
     * @param mediaType Optional MediaType; if null, the function attempts to detect PNG vs JPG.
     */
    fun cover(
        base64: Base64Image,
        mediaType: MediaType? = null
    ) {
        if (base64.isEmpty)
            return

        // Strip Data URI prefix if present
        val cleanBase64 = base64.value!!.substringAfter("base64,", base64.value)

        // Decode string to raw bytes
        val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)

        // Heuristic detection of image type via Base64 signatures
        val type = mediaType ?: when {
            cleanBase64.startsWith("iVBOR") -> MediatypeService.PNG
            cleanBase64.startsWith("/9j/") -> MediatypeService.JPG
            else -> MediatypeService.PNG // default fallback
        }

        book.coverImage = Resource(bytes, type)
    }

    /**
     * Defines a new chapter in the book.
     * * @param title The title of the section/chapter as it appears in the Table of Contents.
     * @param block A DSL scope for [ChapterBuilder] to define the chapter content.
     */
    fun chapter(
        title: String,
        block: ChapterBuilder.() -> Unit
    ) {
        val builder = ChapterBuilder(title).apply(block)
        book.addSection(title, builder.build())
    }

    /** Adds a standalone image resource to the book (e.g., for use inside chapters). */
    fun image(
        bytes: ByteArray,
        mediaType: MediaType = MediatypeService.PNG
    ) {
        book.resources.add(Resource(bytes, mediaType))
    }

    /** Finalizes the construction and returns the [Book] instance. */
    fun build(): Book = book
}