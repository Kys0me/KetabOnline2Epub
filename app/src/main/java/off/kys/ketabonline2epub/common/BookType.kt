package off.kys.ketabonline2epub.common

import off.kys.ketabonline2epub.domain.model.BookId

sealed class BookType(val bookId: BookId) {
    class PDF(bookId: BookId) : BookType(bookId) {
        companion object {
            const val NAME = "PDF"
        }
    }
    class EPUB(bookId: BookId) : BookType(bookId) {
        companion object {
            const val NAME = "EPUB"
        }
    }

    val name: String
        get() = this::class.simpleName ?: throw IllegalStateException("The class name is not supposed to be null")

    companion object {
        /**
         * Replicates Enum.valueOf() behavior.
         * Maps the stored string name back to the sealed class implementation.
         */
        fun valueOf(name: String, bookId: BookId): BookType {
            return when (name.uppercase()) {
                "PDF" -> PDF(bookId)
                "EPUB" -> EPUB(bookId)
                else -> throw IllegalArgumentException("No BookType constant for name: $name")
            }
        }
    }
}