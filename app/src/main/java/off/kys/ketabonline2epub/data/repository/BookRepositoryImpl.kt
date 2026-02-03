package off.kys.ketabonline2epub.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import off.kys.ketabonline2epub.common.Constants
import off.kys.ketabonline2epub.domain.model.Base64Image
import off.kys.ketabonline2epub.domain.model.BookData
import off.kys.ketabonline2epub.domain.model.BookId
import off.kys.ketabonline2epub.domain.model.BookIndex
import off.kys.ketabonline2epub.domain.model.BookItem
import off.kys.ketabonline2epub.domain.model.BookPage
import off.kys.ketabonline2epub.domain.repository.BookRepository
import off.kys.ketabonline2epub.util.downloadFile
import off.kys.ketabonline2epub.util.extensions.Empty
import off.kys.ketabonline2epub.util.extensions.cacheDir
import off.kys.ketabonline2epub.util.extensions.encodeUrl
import off.kys.ketabonline2epub.util.extensions.nextNullableString
import off.kys.ketabonline2epub.util.extensions.readUrlAsText
import off.kys.ketabonline2epub.util.extensions.safeArray
import off.kys.ketabonline2epub.util.extensions.safeString
import off.kys.ketabonline2epub.util.extensions.toPlainText
import off.kys.ketabonline2epub.util.readTextFromUrl
import off.kys.ketabonline2epub.util.unzip
import java.io.FileReader

private const val TAG = "BookRepositoryImpl"

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
        val url = "${Constants.API_BASE_URL}/books/${bookId.value}/index"

        Log.d(TAG, "Fetching index from: $url")

        try {
            val indexJson = readTextFromUrl(url)
            val rootIndex = JsonParser.parseString(indexJson).asJsonObject
            val status = rootIndex["status"].asBoolean
            val code = rootIndex["code"].asInt

            Log.d(TAG, "Index parsing. status: $status, code: $code")

            Log.d(TAG, "Json text:\n $indexJson")

            if (!status || code != 200) {
                Log.w(TAG, "Stopping index fetch. Status: $status, Code: $code")
                return bookIndices
            }

            val data = rootIndex["data"].asJsonArray

            if (data.isEmpty) {
                Log.w(TAG, "Stopping index fetch. Data is empty")
                return bookIndices
            }

            data.forEach { index ->
                val obj = index.asJsonObject

                bookIndices += BookIndex(
                    title = obj["title"].asString,
                    pageId = obj["page_id"].asInt,
                    page = obj["page"].asInt,
                    part = obj["part_name"]?.safeString()?.toIntOrNull() ?: run {
                        Log.w(TAG, "Invalid part name: ${obj["part_name"]}")
                        1
                    }
                )
                Log.d(TAG, "Parsed index: ${bookIndices.last()}")
            }
        } catch (e: Exception) {
            Log.e(TAG,  "Error fetching index", e)
        }

        return bookIndices
    }

    override suspend fun getBookData(bookId: BookId): BookData {
        val bookPages = mutableListOf<BookPage>()
        val zipPath = context.cacheDir("${bookId.value}.data.zip")

        try {
            val dataUrl = "${Constants.STORAGE_BASE_URL}/${bookId.value}/${bookId.value}.data.zip"
            Log.d(TAG, "Downloading data from: $dataUrl")
            downloadFile(dataUrl, zipPath.absolutePath)

            val dataJsonPath = unzip(zipPath) ?: throw RuntimeException("Failed to unzip book data.")

            if (!dataJsonPath.exists()) {
                throw IllegalStateException("Expected data file $dataJsonPath not found.")
            }

            Log.d(TAG, "Unzip successful. Streaming JSON data...")

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
                        "title" -> bookTitle = reader.nextNullableString() ?: throw IllegalStateException("This book has no title")
                        "description" -> bookDescription = reader.nextNullableString()
                        "info" -> bookInfo = reader.nextNullableString() // This fixes the crash
                        "image_file" -> bookCover = reader.nextNullableString()
                        "authors" -> {
                            val authors = mutableListOf<String>()
                            reader.beginArray()
                            while (reader.hasNext()) {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    if (reader.nextName() == "name") {
                                        authors.add(reader.nextNullableString() ?: String.Empty())
                                    } else {
                                        reader.skipValue()
                                    }
                                }
                                reader.endObject()
                            }
                            reader.endArray()
                            bookAuthor = authors.joinToString()
                        }
                        "pages" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                val pageElement = JsonParser.parseReader(reader).asJsonObject
                                bookPages.add(parseBookPage(pageElement))
                            }
                            reader.endArray()
                        }
                        else -> reader.skipValue()
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
            Log.d(TAG, "Failed to retrieve book data", e)
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