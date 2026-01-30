package off.kys.ketabonline2epub.util.epub_builder.dsl

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubWriter
import off.kys.ketabonline2epub.util.epub_builder.EpubBuilder
import java.io.File

/**
 * Top-level entry point for the EPUB DSL.
 *
 * This function instantiates an [EpubBuilder], applies the provided configuration
 * block, and returns a compiled [Book] object.
 *
 * @param title The title of the book.
 * @param block The DSL configuration block.
 * @return A finalized [Book] instance.
 */
fun epub(
    title: String,
    block: EpubBuilder.() -> Unit
): Book = EpubBuilder(title).apply(block).build()

/**
 * Extension function to write a [Book] instance to a physical [File].
 *
 * It utilizes [EpubWriter] to package all resources (XHTML, CSS, Images)
 * into the OCF (Open Container Format) structure required for .epub files.
 *
 * @param file The target destination on the filesystem.
 */
fun Book.writeTo(file: File) {
    EpubWriter().write(this, file.outputStream())
}