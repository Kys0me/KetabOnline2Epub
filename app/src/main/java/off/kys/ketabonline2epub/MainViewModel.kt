package off.kys.ketabonline2epub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.logging.Level

class MainViewModel(
    private val bookRepository: BookRepository,
    private val epubConverterRepository: EpubConverterRepository
) : ViewModel() {

    fun searchBook(bookName: String, page: Int = 1, onResult: (List<BookItem>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            onResult(bookRepository.searchBooks(bookName, page))
        }
    }

    fun downloadBook(bookId: BookId, onResult: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            logger.level = Level.ALL
            logger.info("Starting EPUB generation for Book ID: $bookId")

            try {
                logger.info("Step 1: Fetching book index...")
                val bookIndex = bookRepository.getBookIndex(bookId)
                logger.info("Fetched ${bookIndex.size} index entries.")

                logger.info("Step 2: Fetching book data (pages)...")
                val bookData = bookRepository.getBookData(bookId)
                logger.info("Fetched data for '${bookData.title}' by ${bookData.author}. Total pages: ${bookData.pages.size}")

                logger.info("Step 3: Organizing chapters...")
                val chapters = epubConverterRepository.buildChapters(
                    indices = bookIndex,
                    pages = bookData.pages,
                    defaultTitle = "بلا عنوان"
                )
                logger.info("Organized content into ${chapters.size} chapters.")

                logger.info("Step 4: Writing EPUB...")
                logger.info("Success! Process completed.")

                onResult(epubConverterRepository.generate(bookData, chapters))
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Process failed due to an unexpected error", e)
                onResult(null)
            }
        }
    }

}