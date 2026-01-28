package off.kys.ketabonline2epub

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.util.logging.Level

class BookRepositoryImpl(
    private val context: Context
) : BookRepository {
    override fun searchBooks(query: String): List<BookItem> {
        TODO("Not yet implemented")
    }

    override fun getBookIndex(bookId: BookId): List<BookIndex> {
        val bookIndices = mutableListOf<BookIndex>()
        var part = 1
        var keepFetching = true

        while (keepFetching) {
            val url = "$API_BASE_URL/books/${bookId.value}/index?part=$part&is_recursive=1"
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

    override fun getBookData(bookId: BookId): BookData {
        val bookPages = mutableListOf<BookPage>()
        val zipPath = context.cacheDir("$bookId.data.zip")
        val dataJsonPath = context.cacheDir("/$bookId.data.json")

        try {
            // Download
            val dataUrl = "$STORAGE_BASE_URL/$bookId/$bookId.data.zip"
            logger.info("Downloading data from: $dataUrl")

            val dataBytes = URI(dataUrl).toURL().readBytes()
            zipPath.writeBytes(dataBytes)
            logger.info("Download complete. Size: ${dataBytes.size} bytes.")

            // Unzip and Process
            if (unzip(zipPath) == 0) {
                logger.info("Unzip successful. Reading JSON data...")

                if (!dataJsonPath.exists()) {
                    throw IllegalStateException("Expected data file $dataJsonPath not found after unzipping.")
                }

                val dataJson = dataJsonPath.readText()
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
                dataJsonPath.delete()
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

    override fun downloadBook(bookId: BookId) {
        TODO("Not yet implemented")
    }

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

        return BookPage(
            id = pageId,
            part = pagePart,
            page = pageIndex,
            content = pageContent
        )
    }
}