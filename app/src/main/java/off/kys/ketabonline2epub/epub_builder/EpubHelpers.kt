package off.kys.ketabonline2epub.epub_builder

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubWriter
import java.io.File

fun epub(
    title: String,
    block: EpubBuilder.() -> Unit
): Book = EpubBuilder(title).apply(block).build()

fun Book.writeTo(file: File) {
    EpubWriter().write(this, file.outputStream())
}

fun Book.writeTo(path: String) {
    writeTo(File(path))
}
