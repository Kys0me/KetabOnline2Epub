package off.kys.ketabonline2epub

@JvmInline
value class BookId(val value: Int) {
    override fun toString(): String = "$value"

    companion object {
        val Empty = BookId(-1)
    }
}