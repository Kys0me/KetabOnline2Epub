package off.kys.ketabonline2epub.data.repository

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import off.kys.ketabonline2epub.common.Constants
import off.kys.ketabonline2epub.common.logger
import off.kys.ketabonline2epub.domain.model.Base64Image
import off.kys.ketabonline2epub.domain.model.BookData
import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.model.BookIndex
import off.kys.ketabonline2epub.domain.model.BookItem
import off.kys.ketabonline2epub.domain.model.BookPage
import off.kys.ketabonline2epub.domain.repository.BookRepository
import off.kys.ketabonline2epub.util.downloadFile
import off.kys.ketabonline2epub.util.extensions.cacheDir
import off.kys.ketabonline2epub.util.extensions.encodeUrl
import off.kys.ketabonline2epub.util.extensions.readUrlAsText
import off.kys.ketabonline2epub.util.extensions.safeArray
import off.kys.ketabonline2epub.util.extensions.safeString
import off.kys.ketabonline2epub.util.extensions.toPlainText
import off.kys.ketabonline2epub.util.unzip
import java.io.FileReader
import java.net.URI
import java.util.logging.Level

class BookRepositoryImpl(
    private val context: Context
) : BookRepository {
    override suspend fun searchBooks(query: String, page: Int): List<BookItem> {
        val books = mutableListOf<BookItem>()
        val apiUrl =
            "https://backend.ketabonline.com/api/v2/books?is_active=1&is_deleted=0&page=1&limit=20&q=${query.encodeUrl()}&scope=titles&sort_field=_score&sort_direction=DESC"

        val jsonText = apiUrl.readUrlAsText()
        val searchResult = JsonParser.parseString(jsonText).asJsonObject
        val data = searchResult["data"].asJsonArray
        val status = searchResult["status"].asBoolean
        val code = searchResult["code"].asInt

        if (!status || code != 200)
            throw IllegalStateException("Search failed with status $status and code $code")

        books += data.map {
            val book = it.asJsonObject
            val author = book["authors"]
                ?.safeArray()
                ?.joinToString { author -> author.asJsonObject["name"].asString }
                ?: ""

            BookItem(
                id = BookId(book["id"].asInt),
                title = book["title"].asString,
                author = author,
                coverUrl = book["image_url"]?.safeString()
            )
        }

        return books
    }

    override suspend fun getBookIndex(bookId: BookId): List<BookIndex> {
        val bookIndices = mutableListOf<BookIndex>()
        var part = 1
        var keepFetching = true

        while (keepFetching) {
            val url = "${Constants.API_BASE_URL}/books/${bookId.value}/index?part=$part&is_recursive=1"
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
                    bookIndices + BookIndex(
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

    override suspend fun getBookData(bookId: BookId): BookData {
        val bookPages = mutableListOf<BookPage>()
        val zipPath = context.cacheDir("${bookId.value}.data.zip")

        try {
            val dataUrl = "${Constants.STORAGE_BASE_URL}/${bookId.value}/${bookId.value}.data.zip"
            logger.info("Downloading data from: $dataUrl")
            downloadFile(dataUrl, zipPath.absolutePath)

            val dataJsonPath = unzip(zipPath) ?: throw RuntimeException("Failed to unzip book data.")

            if (!dataJsonPath.exists()) {
                throw IllegalStateException("Expected data file $dataJsonPath not found.")
            }

            logger.info("Unzip successful. Streaming JSON data...")

            // Local variables to hold data as we stream
            var bookTitle = ""
            var bookAuthor = ""
            var bookCover: String? = null
            var bookDescription: String? = null
            var bookInfo: String? = null

            // Use JsonReader for streaming to prevent OOM
            JsonReader(FileReader(dataJsonPath)).use { reader ->
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "title" -> bookTitle = reader.nextString()
                        "description" -> bookDescription = reader.nextString()
                        "info" -> bookInfo = reader.nextString()
                        "image_file" -> bookCover = if (reader.peek() != JsonToken.NULL) reader.nextString() else { reader.nextNull(); null }
                        "authors" -> {
                            val authors = mutableListOf<String>()
                            reader.beginArray()
                            while (reader.hasNext()) {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    if (reader.nextName() == "name") authors.add(reader.nextString())
                                    else reader.skipValue()
                                }
                                reader.endObject()
                            }
                            reader.endArray()
                            bookAuthor = authors.joinToString()
                        }
                        "pages" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                // Using your existing parse logic but converting current object
                                val pageElement = JsonParser.parseReader(reader).asJsonObject
                                bookPages.add(parseBookPage(pageElement))
                            }
                            reader.endArray()
                        }
                        else -> reader.skipValue() // Ignore unknown fields
                    }
                }
                reader.endObject()
            }

            dataJsonPath.delete()

            return BookData(
                cover = Base64Image(bookCover),
                description = bookDescription,
                info = bookInfo,
                author = bookAuthor,
                title = bookTitle,
                pages = bookPages
            )

        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to retrieve book data", e)
            throw e
        }
    }

    private fun parseBookPage(page: JsonObject): BookPage {
        val pageId = page["id"].asInt
        val pageIndex = page["page"].asInt
        val pageContent = page["content"].asString.toPlainText()

        // Complex part parsing logic
        val rawPart = page["part"]
        val pagePart = when {
            rawPart == null || rawPart.isJsonNull -> null
            rawPart.isJsonObject -> rawPart.asJsonObject["name"]?.asString?.toIntOrNull()
            rawPart.isJsonArray -> rawPart.asJsonArray.firstOrNull()?.asJsonObject?.get("name")?.asString?.toIntOrNull()
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