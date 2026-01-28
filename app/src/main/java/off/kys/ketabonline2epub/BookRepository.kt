package off.kys.ketabonline2epub

interface BookRepository {

    fun searchBooks(query: String): List<BookItem>

    fun getBookIndex(bookId: BookId): List<BookIndex>

    fun getBookData(bookId: BookId): BookData

    fun downloadBook(bookId: BookId)

}