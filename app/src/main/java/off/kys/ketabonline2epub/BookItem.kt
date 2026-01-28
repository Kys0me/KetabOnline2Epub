package off.kys.ketabonline2epub

data class BookItem(
    val id: BookId,
    val title: String,
    val author: String,
    val coverUrl: String
)

@JvmInline
value class BookId(val value: Int)