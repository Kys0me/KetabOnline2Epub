package off.kys.ketabonline2epub.domain.model

data class BookData(
    val cover: Base64Image,
    val description: String?,
    val info: String?,
    val title: String,
    val pages: List<BookPage>,
    val author: String
)