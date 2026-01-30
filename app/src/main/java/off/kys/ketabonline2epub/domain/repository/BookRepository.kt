package off.kys.ketabonline2epub.domain.repository

import off.kys.ketabonline2epub.domain.model.BookData
import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.model.BookIndex
import off.kys.ketabonline2epub.domain.model.BookItem

interface BookRepository {

    suspend fun searchBooks(query: String, page: Int): List<BookItem>

    suspend fun getBookIndex(bookId: BookId): List<BookIndex>

    suspend fun getBookData(bookId: BookId): BookData
}