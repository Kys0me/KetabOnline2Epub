package off.kys.ketabonline2epub

import androidx.lifecycle.ViewModel

class MainViewModel(
    private val bookRepository: BookRepository,
    private val epubConverterRepository: EpubConverterRepository
): ViewModel() {
    private val searchResults = mutableListOf<BookItem>()

    fun searchBook(bookName: String) {

    }


}