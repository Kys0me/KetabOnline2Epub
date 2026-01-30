package off.kys.ketabonline2epub.domain.model

@JvmInline
value class Base64Image(val value: String?) {
    val isEmpty: Boolean
        get() = value.isNullOrEmpty()

    val isNotEmpty: Boolean
        get() = !isEmpty

    operator fun invoke(): String? = value
}