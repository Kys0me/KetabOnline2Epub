package off.kys.ketabonline2epub.domain.model

data class BookPage(
    val id: Int,
    val page: Int,
    val part: Int,
    val content: String
)