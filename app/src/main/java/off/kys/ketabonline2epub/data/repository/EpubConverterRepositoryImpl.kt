package off.kys.ketabonline2epub.data.repository

import android.content.Context
import android.util.Log
import off.kys.ketabonline2epub.domain.model.BookData
import off.kys.ketabonline2epub.domain.model.BookIndex
import off.kys.ketabonline2epub.domain.model.BookPage
import off.kys.ketabonline2epub.domain.model.TableOfContent
import off.kys.ketabonline2epub.domain.repository.EpubConverterRepository
import off.kys.ketabonline2epub.util.epub_builder.dsl.epub
import off.kys.ketabonline2epub.util.epub_builder.dsl.writeTo
import off.kys.ketabonline2epub.util.extensions.cacheDir
import java.io.File

private const val TAG = "EpubConverterRepository"

class EpubConverterRepositoryImpl(
    private val context: Context
) : EpubConverterRepository {

    override fun generate(
        bookData: BookData,
        chapters: List<TableOfContent>
    ): File {
        Log.d(TAG, "Starting EPUB generation for: ${bookData.title}")
        val outputFile = context.cacheDir("${bookData.title}.epub")

        try {
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
                            append(bookData.description)
                            append("<br/>")
                        }
                        if (bookData.info != null) {
                            append("حول الكتاب: ")
                            append(bookData.info)
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
                                append(page.content)
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

            Log.i(TAG, "Successfully generated EPUB: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate EPUB for ${bookData.title}", e)
        }

        return outputFile
    }

    override fun buildChapters(
        indices: List<BookIndex>,
        pages: List<BookPage>,
        defaultTitle: String
    ): List<TableOfContent> {
        Log.d(TAG, "Building chapters: Indices count = ${indices.size}, Pages count = ${pages.size}")

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
                Log.v(TAG, "Adding prefix chapter with ${beforeFirst.size} pages")
                chapters.add(TableOfContent(title = defaultTitle, pages = beforeFirst))
            }
        }

        // 2. Real chapters defined in the index
        for (i in sortedIndices.indices) {
            val current = sortedIndices[i]
            val next = sortedIndices.getOrNull(i + 1)

            val chapterPages = takePagesUntil(next?.pageId)

            if (chapterPages.isNotEmpty()) {
                Log.v(TAG, "Mapping chapter: ${current.title} (${chapterPages.size} pages)")
                chapters.add(TableOfContent(title = current.title, pages = chapterPages))
            } else {
                Log.w(TAG, "Chapter '${current.title}' has no associated pages.")
            }
        }

        // 3. Remaining pages after the last chapter
        val remaining = takePagesUntil(null)
        if (remaining.isNotEmpty()) {
            Log.v(TAG, "Adding remaining ${remaining.size} pages to trailing chapter")
            chapters.add(TableOfContent(title = defaultTitle, pages = remaining))
        }

        Log.d(TAG, "Finished building ${chapters.size} total chapters")
        return chapters
    }
}