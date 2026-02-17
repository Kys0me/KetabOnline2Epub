package off.kys.ketabonline2epub.common

sealed interface BookType {
    data object PDF : BookType

    data object EPUB : BookType

    val name: String
        get() = this::class.simpleName
            ?: throw IllegalStateException("The class name is not supposed to be null")
}