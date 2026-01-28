package off.kys.ketabonline2epub

import android.content.Context
import off.kys.ketabonline2epub.epub_builder.epub
import off.kys.ketabonline2epub.epub_builder.writeTo
import java.io.File

class EpubConverterRepositoryImpl(
    private val context: Context
) : EpubConverterRepository {
    override fun generate(
        bookData: BookData,
        chapters: List<TableOfContent>
    ): File {
        val outputFile = context.cacheDir("${bookData.title}.epub")

        epub(bookData.title) {
            author(bookData.author)
            language("ar")

            if (bookData.cover.isNotEmpty) {
                cover(bookData.cover)
            }

            chapter(title = "بطاقة الكتاب") {
                p {
                    append("عنوان الكتاب: ")
                    append(bookData.title)
                    append("<br/>")
                    append("عدد الصفحات: ")
                    append(bookData.pages.size.toString())
                    append("<br/>")
                    append("المؤلف: ")
                    append(bookData.author)
                    append("<br/>")
                    if (bookData.description != null) {
                        append("وصف الكتاب: ")
                        append(bookData.description.normalize())
                        append("<br/>")
                    }
                    if (bookData.info != null) {
                        append("حول الكتاب: ")
                        append(bookData.info.normalize())
                        append("<br/>")
                    }
                }
            }

            if (bookData.cover.isNotEmpty)
                chapter("الغلاف") {
                    img(
                        src = this@epub.bookCover?.href ?: return@chapter,
                        alt = "غلاف الكتاب"
                    )
                }

            chapters.forEach { ch ->
                chapter(title = ch.title) {
                    ch.pages.forEach { page ->
                        p {
                            append(page.content.normalize())
                            append(" [م ")
                            append(page.part)
                            append(" ص ")
                            append(page.page)
                            append(" ]")
                        }
                        br()
                    }
                }
            }
        }.writeTo(outputFile)

        logger.info("EPUB saved to: $outputFile")
        return outputFile
    }

    override fun buildChapters(
        indices: List<BookIndex>,
        pages: List<BookPage>,
        defaultTitle: String
    ): List<TableOfContent> {
        // Sorting to ensure linear processing
        val sortedIndices = indices.sortedBy { it.pageId }
        val sortedPages = pages.sortedBy { it.id }

        val chapters = mutableListOf<TableOfContent>()
        var pageCursor = 0

        // Helper to grab pages until a specific ID is reached
        fun takePagesUntil(limitPageId: Int?): List<BookPage> {
            val collected = mutableListOf<BookPage>()
            while (
                pageCursor < sortedPages.size &&
                (limitPageId == null || sortedPages[pageCursor].id < limitPageId)
            ) {
                collected += sortedPages[pageCursor]
                pageCursor++
            }
            return collected
        }

        // 1. Pages before the first explicit chapter (Introduction/Untitled)
        if (sortedIndices.isNotEmpty()) {
            val beforeFirst = takePagesUntil(sortedIndices.first().pageId)
            if (beforeFirst.isNotEmpty()) {
                chapters += TableOfContent(title = defaultTitle, pages = beforeFirst)
            }
        }

        // 2. Real chapters defined in the index
        for (i in sortedIndices.indices) {
            val current = sortedIndices[i]
            val next = sortedIndices.getOrNull(i + 1)

            val chapterPages = takePagesUntil(next?.pageId)

            if (chapterPages.isNotEmpty()) {
                chapters += TableOfContent(title = current.title, pages = chapterPages)
            }
        }

        // 3. Remaining pages after the last chapter
        val remaining = takePagesUntil(null)
        if (remaining.isNotEmpty()) {
            chapters += TableOfContent(title = defaultTitle, pages = remaining)
        }

        return chapters
    }
}