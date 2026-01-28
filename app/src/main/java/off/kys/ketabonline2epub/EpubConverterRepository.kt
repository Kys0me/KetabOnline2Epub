package off.kys.ketabonline2epub

import java.io.File

interface EpubConverterRepository {

    fun generate(bookData: BookData, chapters: List<TableOfContent>): File

    fun buildChapters(
        indices: List<BookIndex>,
        pages: List<BookPage>,
        defaultTitle: String = "Untitled",
    ): List<TableOfContent>

}