package off.kys.ketabonline2epub.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import off.kys.ketabonline2epub.R
import off.kys.ketabonline2epub.common.Constants
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
import off.kys.ketabonline2epub.util.extensions.nextNullableString
import off.kys.ketabonline2epub.util.extensions.toPlainText
import off.kys.ketabonline2epub.util.extensions.toURL
import off.kys.ketabonline2epub.util.unzip
import java.io.FileReader
import java.io.InputStreamReader
import java.net.URL

private const val TAG = "BookRepositoryImpl"

class BookRepositoryImpl(
    private val context: Context
) : BookRepository {

    // MARK: - Search Books
    override suspend fun searchBooks(query: String, page: Int): List<BookItem> {
        val books = mutableListOf<BookItem>()
        val apiUrl =
            "https://backend.ketabonline.com/api/v2/books?is_active=1&is_deleted=0&page=1&limit=20&q=${query.encodeUrl()}&scope=titles&sort_field=_score&sort_direction=DESC"

        val inputStream = withContext(Dispatchers.IO) { URL(apiUrl).openStream() }

        withContext(Dispatchers.IO) {
            JsonReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                reader.beginObject()
                var status = false
                var code = 0

                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "status" -> status = reader.nextBoolean()
                        "code" -> code = reader.nextInt()
                        "data" -> {
                            if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    books.add(readBookItem(reader))
                                }
                                reader.endArray()
                            } else {
                                reader.skipValue()
                            }
                        }

                        else -> reader.skipValue()
                    }
                }
                reader.endObject()

                if (!status || code != 200) {
                    throw IllegalStateException("Search failed with status $status and code $code")
                }
            }
        }
        return books
    }

    // MARK: - Get Book Index
    override suspend fun getBookIndex(bookId: BookId): List<BookIndex> {
        val url = "${Constants.API_BASE_URL}/books/${bookId.value}/index".toURL()
        Log.d(TAG, "Fetching index from: $url")

        return try {
            withContext(Dispatchers.IO) {
                url.openStream().use { inputStream ->
                    JsonReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        val result = mutableListOf<BookIndex>()
                        var status = false
                        var code = 0

                        reader.beginObject()

                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "status" -> status = reader.nextBoolean()
                                "code" -> code = reader.nextInt()
                                "data" -> {
                                    if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                                        reader.beginArray()
                                        while (reader.hasNext()) {
                                            result.add(readBookIndex(reader))
                                        }
                                        reader.endArray()
                                    } else {
                                        reader.skipValue()
                                    }
                                }

                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        if (!status || code != 200) {
                            Log.w(TAG, "Stopping index fetch. Status: $status, Code: $code")
                            return@use emptyList()
                        }

                        Log.d(TAG, "Parsed ${result.size} index items")
                        result
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching index", e)
            emptyList()
        }
    }

    // MARK: - Get Book Data
    override suspend fun getBookData(bookId: BookId): BookData {
        val zipPath = context.cacheDir("${bookId.value}.data.zip")
        val dataUrl = "${Constants.STORAGE_BASE_URL}/${bookId.value}/${bookId.value}.data.zip"

        try {
            Log.d(TAG, "Downloading data from: $dataUrl")
            downloadFile(dataUrl, zipPath.absolutePath)

            val dataJsonPath = unzip(zipPath)
                ?: throw RuntimeException("Failed to unzip book data.")

            if (!dataJsonPath.exists()) {
                throw IllegalStateException("Expected data file $dataJsonPath not found.")
            }

            Log.d(TAG, "Unzip successful. Streaming JSON data...")

            return withContext(Dispatchers.IO) {
                JsonReader(FileReader(dataJsonPath)).use { reader ->
                    parseBookDataStream(reader)
                }
            }.also {
                dataJsonPath.delete() // Cleanup
                Log.d(TAG, "Book data parsed successfully")
            }

        } catch (e: Exception) {
            Log.d(TAG, "Failed to retrieve book data", e)
            throw e
        }
    }

    // MARK: - Parsing Helpers

    /**
     * Reads a book page from the JSON reader.
     *
     * @param reader The JSON reader.
     * @return The parsed book page.
     */
    private fun readBookPage(reader: JsonReader): BookPage {
        var pageId = 0
        var pageIndex = 0
        var content = ""
        var part: Int? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> pageId = reader.nextInt()
                "page" -> pageIndex = reader.nextInt()
                "content" -> content = reader.nextString().toPlainText()
                "part" -> {
                    part = when (reader.peek()) {
                        JsonToken.NULL -> {
                            reader.nextNull()
                            null
                        }

                        JsonToken.BEGIN_OBJECT -> {
                            readPartObject(reader)
                        }

                        JsonToken.BEGIN_ARRAY -> {
                            reader.beginArray()
                            val value = if (reader.hasNext()) readPartObject(reader) else null
                            reader.endArray()
                            value
                        }

                        else -> {
                            reader.skipValue()
                            null
                        }
                    }
                }

                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return BookPage(
            id = pageId,
            part = part ?: 1,
            page = pageIndex,
            content = content
        )
    }

    /**
     * Reads a part object from the JSON reader.
     *
     * @param reader The JSON reader.
     * @return The parsed part object.
     */
    private fun readPartObject(reader: JsonReader): Int? {
        var part: Int? = null

        reader.beginObject()
        while (reader.hasNext()) {
            if (reader.nextName() == "name") {
                part = reader.nextNullableString()?.toIntOrNull()
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()

        return part
    }

    /**
     * Extracts the main logic for parsing the massive book data JSON file.
     */
    private fun parseBookDataStream(reader: JsonReader): BookData {
        var bookTitle = ""
        var bookAuthor = ""
        var pdfUrl: String? = null
        var bookCover: String? = null
        var bookDescription: String? = null
        var bookInfo: String? = null
        val bookPages = mutableListOf<BookPage>()

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "title" -> bookTitle = reader.nextNullableString()
                    ?: throw IllegalStateException("This book has no title")

                "description" -> bookDescription = reader.nextNullableString()
                "info" -> bookInfo = reader.nextNullableString()
                "image_file" -> bookCover = reader.nextNullableString()
                "authors" -> bookAuthor = readAuthors(reader)
                "pages" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        bookPages.add(readBookPage(reader))
                    }
                    reader.endArray()
                }

                "files" -> {
                    // Reuse the logic to extract PDF url from 'files' object
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        val foundUrl = readPdfUrl(reader)
                        if (!foundUrl.isNullOrEmpty()) pdfUrl = foundUrl
                    } else {
                        reader.skipValue()
                    }
                }

                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return BookData(
            cover = Base64Image(bookCover),
            description = bookDescription,
            info = bookInfo,
            author = bookAuthor,
            title = bookTitle,
            pages = bookPages,
            pdfUrl = pdfUrl
        )
    }

    /**
     * Shared helper to parse book indexes.
     * Looks for: "title": "Book Title",
     *            "page_id": 123,
     *            "page": 1,
     *            "part_name": "Part 1"
     *            "part": 1
     */
    private fun readBookIndex(reader: JsonReader): BookIndex {
        var title = ""
        var pageId = 0
        var page = 1
        var part = 1

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "title" -> title = reader.nextString()
                "page_id" -> pageId = reader.nextInt()
                "page" -> {
                    page = if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull()
                        1
                    } else {
                        reader.nextInt()
                    }
                }

                "part_name" -> part = reader.nextNullableString()?.toIntOrNull() ?: 1

                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return BookIndex(
            title = title,
            pageId = pageId,
            page = page,
            part = part
        )
    }

    /**
     * Shared helper to parse book items.
     * Looks for: "id": 123
     *            "title": "Book Title"
     *            "image_url": "https://example.com/cover.jpg"
     *            "authors": [{"name": "Author 1"}, {"name": "Author 2"}]
     *            "files": { "pdf": { "url": "https://example.com/book.pdf" } }
     */
    private fun readBookItem(reader: JsonReader): BookItem {
        var id = 0
        var title = ""
        var authors = ""
        var coverUrl: String? = null
        var pdfAvailable = false

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextInt()
                "title" -> title = reader.nextString()
                "image_url" -> coverUrl = if (reader.peek() == JsonToken.NULL) {
                    @Suppress("KotlinConstantConditions")
                    reader.nextNull().run { null }
                } else {
                    reader.nextString()
                }

                "authors" -> {
                    if (reader.peek() == JsonToken.BEGIN_ARRAY) authors = readAuthors(reader)
                    else reader.skipValue()
                }

                "files" -> {
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) pdfAvailable =
                        !readPdfUrl(reader).isNullOrEmpty()
                    else reader.skipValue()
                }

                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return BookItem(
            id = BookId(id),
            title = title,
            author = authors,
            coverUrl = coverUrl,
            hasPdf = pdfAvailable
        )
    }

    /**
     * Shared helper to parse author arrays.
     * Handles the specific structure: [{"name": "Author 1"}, {"name": "Author 2"}]
     */
    private fun readAuthors(reader: JsonReader): String {
        val names = mutableListOf<String>()
        reader.beginArray()
        while (reader.hasNext()) {
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.nextName() == "name") {
                    reader.nextNullableString()?.let { names.add(it) }
                        ?: context.getString(R.string.unknown_author).let { names.add(it) }
                } else {
                    reader.skipValue()
                }
            }
            reader.endObject()
        }
        reader.endArray()
        return names.joinToString(", ")
    }

    /**
     * Shared helper to parse PDF URL from the "files" object.
     * Looks for: "files": { "pdf": { "url": "..." } }
     */
    private fun readPdfUrl(reader: JsonReader): String? {
        var pdfUrl: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            if (reader.nextName() == "pdf") {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                } else {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        if (reader.nextName() == "url") {
                            pdfUrl = reader.nextNullableString()
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endObject()
                }
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()
        return pdfUrl
    }

}