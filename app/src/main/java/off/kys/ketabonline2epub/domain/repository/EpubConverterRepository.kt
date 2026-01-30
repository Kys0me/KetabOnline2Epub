package off.kys.ketabonline2epub.domain.repository

import off.kys.ketabonline2epub.domain.model.BookData
import off.kys.ketabonline2epub.domain.model.BookIndex
import off.kys.ketabonline2epub.domain.model.BookPage
import off.kys.ketabonline2epub.domain.model.TableOfContent
import java.io.File

interface EpubConverterRepository {

    fun generate(bookData: BookData, chapters: List<TableOfContent>): File

    fun buildChapters(
        indices: List<BookIndex>,
        pages: List<BookPage>,
        defaultTitle: String,
    ): List<TableOfContent>

}