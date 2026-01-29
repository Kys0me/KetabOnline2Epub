package off.kys.ketabonline2epub.save_file

enum class DocumentType(val mimeType: String) {
    ALL("*/*"),
    TEXT("text/plain"),
    AUDIO("audio/*"),
    VIDEO("video/*"),
    PDF("application/pdf"),
    EPUB("application/epub+zip"),
    IMAGE_PNG("image/png"),
    IMAGE_JPG("image/jpeg"),
    JSON("application/json"),
    BINARY("application/octet-stream")
}