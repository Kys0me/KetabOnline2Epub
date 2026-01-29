package off.kys.ketabonline2epub

interface BookRepository {

    suspend fun searchBooks(query: String, page: Int): List<BookItem>

    suspend fun getBookIndex(bookId: BookId): List<BookIndex>

    suspend fun getBookData(bookId: BookId): BookData
}