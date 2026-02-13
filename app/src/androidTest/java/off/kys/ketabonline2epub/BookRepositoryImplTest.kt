package off.kys.ketabonline2epub

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import off.kys.ketabonline2epub.data.repository.BookRepositoryImpl
import off.kys.ketabonline2epub.data.repository.EpubConverterRepositoryImpl
import off.kys.ketabonline2epub.domain.model.BookId
import org.junit.Test

class BookRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val bookId = BookId(6157)

    @Test
    fun testSearchBooks() = runBlocking {
        val repository = BookRepositoryImpl(context)
        val query = "الرفق بالزوجات"
        repository.searchBooks(query, 1).forEach(::println)
        assert(true)
    }

    @Test
    fun testBookDownloading() = runBlocking {
        val bookRepository = BookRepositoryImpl(context)
        val epubConverterRepository = EpubConverterRepositoryImpl(context)

        // Step 1 & 2: Data Acquisition
        val bookIndex = bookRepository.getBookIndex(bookId)
        val bookData = bookRepository.getBookData(bookId)

        // Step 3: Transformation
        val chapters = epubConverterRepository.buildChapters(
            indices = bookIndex,
            pages = bookData.pages,
            defaultTitle = context.getString(R.string.default_untitled)
        )

        // Step 4: Final Generation
        val file = epubConverterRepository.generate(bookData, chapters)
        assert(file.length() > 3771) {
            """
                The downloaded file is likely incomplete
            """.trimIndent()
        }
    }

    @Test
    fun testGettingPdf() = runBlocking {
        val bookRepository = BookRepositoryImpl(context)

        assert(bookRepository.getBookData(bookId).pdfUrl != null)
    }

}