package off.kys.ketabonline2epub.domain.model

data class BookIndex(
    val title: String,
    val pageId: Int,
    val page: Int,
    val part: Int = 1,
)