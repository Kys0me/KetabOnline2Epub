package off.kys.ketabonline2epub.domain.model

data class BookItem(
    val id: BookId,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val hasPdf: Boolean
)