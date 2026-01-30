package off.kys.ketabonline2epub.domain.model

data class TableOfContent(
    val title: String,
    val pages: List<BookPage>
)