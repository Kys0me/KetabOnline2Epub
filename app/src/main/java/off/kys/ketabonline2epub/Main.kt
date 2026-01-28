package off.kys.ketabonline2epub

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import off.kys.ketabonline2epub.epub_builder.epub
import off.kys.ketabonline2epub.epub_builder.writeTo
import java.io.File
import java.net.URI
import java.util.logging.Level
import java.util.logging.Logger

// --- Constants ---
const val API_BASE_URL = "https://backend.ketabonline.com/api/v2"
const val STORAGE_BASE_URL = "https://s2.ketabonline.com/books"

// --- Logger Setup ---
val logger: Logger = Logger.getLogger("EpubBuilder")

// --- Main Entry Point ---
fun main() {
    val bookId = 92964

    logger.level = Level.ALL
    logger.info("Starting EPUB generation for Book ID: $bookId")

    try {
        logger.info("Step 1: Fetching book index...")
        val bookIndex = getBookIndex(bookId)
        logger.info("Fetched ${bookIndex.size} index entries.")

        logger.info("Step 2: Fetching book data (pages)...")
        val bookData = getBookData(bookId)
        logger.info("Fetched data for '${bookData.title}' by ${bookData.author}. Total pages: ${bookData.pages.size}")

        logger.info("Step 3: Organizing chapters...")
        val chapters = buildChapters(
            indices = bookIndex,
            pages = bookData.pages,
            defaultTitle = "بلا عنوان"
        )
        logger.info("Organized content into ${chapters.size} chapters.")

        logger.info("Step 4: Writing EPUB...")
        generateEpub(bookData, chapters)

        logger.info("Success! Process completed.")
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Process failed due to an unexpected error", e)
    }
}

// --- Core Logic ---

fun generateEpub(bookData: BookData, chapters: List<TableOfContent>) {
    val outputFilename = "./${bookData.title}.epub"

    epub(bookData.title) {
        author(bookData.author)
        language("ar")

        if (bookData.cover.isNotEmpty) {
            cover(bookData.cover)
        }

        chapter(title = "بطاقة الكتاب") {
            p {
                append("عنوان الكتاب: ")
                append(bookData.title)
                append("<br/>")
                append("عدد الصفحات: ")
                append(bookData.pages.size.toString())
                append("<br/>")
                append("المؤلف: ")
                append(bookData.author)
                append("<br/>")
                if (bookData.description != null) {
                    append("وصف الكتاب: ")
                    append(bookData.description.normalize())
                    append("<br/>")
                }
                if (bookData.info != null) {
                    append("حول الكتاب: ")
                    append(bookData.info.normalize())
                    append("<br/>")
                }
            }
        }

        if (bookData.cover.isNotEmpty)
            chapter("الغلاف") {
                img(
                    src = this@epub.bookCover?.href ?: return@chapter,
                    alt = "غلاف الكتاب"
                )
            }

        chapters.forEach { ch ->
            chapter(title = ch.title) {
                ch.pages.forEach { page ->
                    p {
                        append(page.content.normalize())
                        append(" [م ")
                        append(page.part)
                        append(" ص ")
                        append(page.page)
                        append(" ]")
                    }
                    br()
                }
            }
        }
    }.writeTo(outputFilename)

    logger.info("EPUB saved to: $outputFilename")
}

fun buildChapters(
    indices: List<BookIndex>,
    pages: List<BookPage>,
    defaultTitle: String = "Untitled",
): List<TableOfContent> {
    // Sorting to ensure linear processing
    val sortedIndices = indices.sortedBy { it.pageId }
    val sortedPages = pages.sortedBy { it.id }

    val chapters = mutableListOf<TableOfContent>()
    var pageCursor = 0

    // Helper to grab pages until a specific ID is reached
    fun takePagesUntil(limitPageId: Int?): List<BookPage> {
        val collected = mutableListOf<BookPage>()
        while (
            pageCursor < sortedPages.size &&
            (limitPageId == null || sortedPages[pageCursor].id < limitPageId)
        ) {
            collected += sortedPages[pageCursor]
            pageCursor++
        }
        return collected
    }

    // 1. Pages before the first explicit chapter (Introduction/Untitled)
    if (sortedIndices.isNotEmpty()) {
        val beforeFirst = takePagesUntil(sortedIndices.first().pageId)
        if (beforeFirst.isNotEmpty()) {
            chapters += TableOfContent(title = defaultTitle, pages = beforeFirst)
        }
    }

    // 2. Real chapters defined in the index
    for (i in sortedIndices.indices) {
        val current = sortedIndices[i]
        val next = sortedIndices.getOrNull(i + 1)

        val chapterPages = takePagesUntil(next?.pageId)

        if (chapterPages.isNotEmpty()) {
            chapters += TableOfContent(title = current.title, pages = chapterPages)
        }
    }

    // 3. Remaining pages after the last chapter
    val remaining = takePagesUntil(null)
    if (remaining.isNotEmpty()) {
        chapters += TableOfContent(title = defaultTitle, pages = remaining)
    }

    return chapters
}

// --- Data Fetching ---

fun getBookIndex(bookId: Int): List<BookIndex> {
    val bookIndices = mutableListOf<BookIndex>()
    var part = 1
    var keepFetching = true

    while (keepFetching) {
        val url = "$API_BASE_URL/books/$bookId/index?part=$part&is_recursive=1"
        logger.fine("Fetching index part $part from: $url")

        try {
            val indexJson = URI(url).toURL().readText()
            val rootIndex = JsonParser.parseString(indexJson).asJsonObject
            val status = rootIndex["status"].asBoolean
            val code = rootIndex["code"].asInt

            if (!status || code != 200) {
                logger.warning("Stopping index fetch. Status: $status, Code: $code")
                keepFetching = false
                continue
            }

            val data = rootIndex["data"].asJsonArray
            if (data.size() == 0) {
                keepFetching = false
                continue
            }

            data.forEach { index ->
                val obj = index.asJsonObject
                bookIndices += BookIndex(
                    title = obj["title"].asString,
                    pageId = obj["page_id"].asInt,
                    page = obj["page"].asInt,
                    part = obj["part"]?.asInt ?: part
                )
            }
            part++

        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error fetching index part $part", e)
            keepFetching = false
        }
    }

    return bookIndices
}

fun getBookData(bookId: Int): BookData {
    val bookPages = mutableListOf<BookPage>()
    val zipPath = "./book_$bookId.data.zip"
    val dataJsonPath = "./$bookId.data.json"
    val zipFile = File(zipPath)

    try {
        // Download
        val dataUrl = "$STORAGE_BASE_URL/$bookId/$bookId.data.zip"
        logger.info("Downloading data from: $dataUrl")

        val dataBytes = URI(dataUrl).toURL().readBytes()
        zipFile.writeBytes(dataBytes)
        logger.info("Download complete. Size: ${dataBytes.size} bytes.")

        // Unzip and Process
        if (unzip(zipFile) == 0) {
            logger.info("Unzip successful. Reading JSON data...")
            val dataFile = File(dataJsonPath)

            if (!dataFile.exists()) {
                throw IllegalStateException("Expected data file $dataJsonPath not found after unzipping.")
            }

            val dataJson = dataFile.readText()
            val rootData = JsonParser.parseString(dataJson).asJsonObject

            val bookTitle = rootData["title"].asString
            val bookAuthor = rootData["authors"].asJsonArray.joinToString {
                it.asJsonObject["name"].asString
            }
            val bookCover = rootData["image_file"]
                ?.takeIf { !it.isJsonNull }
                ?.asString
            val bookDescription = rootData["description"]?.asString
            val bookInfo = rootData["info"]?.asString

            val pages = rootData["pages"].asJsonArray

            pages.forEach {
                val page = it.asJsonObject
                bookPages += parseBookPage(page)
            }

            // Cleanup JSON file
            dataFile.delete()
            return BookData(
                cover = Base64Image(bookCover),
                description = bookDescription,
                info = bookInfo,
                author = bookAuthor,
                title = bookTitle,
                pages = bookPages
            )
        } else {
            throw RuntimeException("Failed to unzip book data.")
        }

    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Failed to retrieve book data", e)
        throw e // Re-throw to stop main process
    }
}

// Helper function to keep getBookData clean
private fun parseBookPage(page: JsonObject): BookPage {
    val pageId = page["id"].asInt
    val pageIndex = page["page"].asInt
    val pageContent = page["content"].asString.toPlainText()

    // Complex part parsing logic
    val rawPart = page["part"]
    val pagePart = when {
        rawPart == null || rawPart.isJsonNull -> null
        rawPart.isJsonObject -> rawPart.asJsonObject["name"]?.asString?.toInt()
        rawPart.isJsonArray -> rawPart.asJsonArray.firstOrNull()?.asJsonObject?.get("name")?.asString?.toInt()
        else -> null
    } ?: 1

    return BookPage(id = pageId, part = pagePart, page = pageIndex, content = pageContent)
}

// --- Utilities ---

