package off.kys.ketabonline2epub.util.epub_builder.dsl

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubWriter
import off.kys.ketabonline2epub.util.epub_builder.EpubBuilder
import java.io.File

fun epub(
    title: String,
    block: EpubBuilder.() -> Unit
): Book = EpubBuilder(title).apply(block).build()

fun Book.writeTo(file: File) {
    EpubWriter().write(this, file.outputStream())
}