package off.kys.ketabonline2epub.domain.repository

import off.kys.ketabonline2epub.domain.model.BookData
import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.model.BookIndex
import off.kys.ketabonline2epub.domain.model.BookItem

interface BookRepository {

    /**
     * Searches for books based on the provided query and page number.
     *
     * @param query The search query.
     * @param page The page number to retrieve.
     * @return A list of [BookItem]
     */
    suspend fun searchBooks(query: String, page: Int): List<BookItem>

    /**
     * Retrieves the index of a book based on its ID.
     *
     * @param bookId The ID of the book.
     * @return A list of [BookIndex]
     */
    suspend fun getBookIndex(bookId: BookId): List<BookIndex>

    /**
     * Retrieves the data of a book based on its ID.
     *
     * @param bookId The ID of the book.
     * @return A [BookData]
     */
    suspend fun getBookData(bookId: BookId): BookData
}