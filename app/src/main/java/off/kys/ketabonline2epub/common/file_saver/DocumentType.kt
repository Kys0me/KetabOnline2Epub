@file:Suppress("unused")

package off.kys.ketabonline2epub.common.file_saver

/**
 * Represents common document types and their associated MIME types.
 * * Used primarily by the file saving system to inform the Android OS
 * which file extensions and suggested apps should be associated with
 * a "Save" operation.
 *
 * @property mimeType The official IANA media type string.
 */
enum class DocumentType(val mimeType: String) {
    /** Matches any file type. */
    ALL("*/*"),

    /** Plain text documents. */
    TEXT("text/plain"),

    /** Any audio format. */
    AUDIO("audio/*"),

    /** Any video format. */
    VIDEO("video/*"),

    /** Portable Document Format. */
    PDF("application/pdf"),

    /** Electronic Publication (EPUB) format, standard for e-books. */
    EPUB("application/epub+zip"),

    /** Portable Network Graphics image. */
    IMAGE_PNG("image/png"),

    /** Joint Photographic Experts Group image. */
    IMAGE_JPG("image/jpeg"),

    /** JavaScript Object Notation. */
    JSON("application/json"),

    /** Unspecified binary data (raw byte stream). */
    BINARY("application/octet-stream")
}