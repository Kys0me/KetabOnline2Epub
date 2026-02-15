package off.kys.ketabonline2epub.util

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import off.kys.ketabonline2epub.domain.model.PdfMetadata
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException

private const val TAG = "PdfProcessor"

/**
 * Processes JSON to PDF and returns the resulting File stored in the app's cache.
 *
 * @param context The application context.
 * @param jsonFile The JSON file containing the metadata.
 *
 * @return The File representing the processed PDF, or null if an error occurs.
 */
fun processJsonToPdf(context: Context, jsonFile: File): File? {
    Log.d(TAG, "Starting processJsonToPdf for file: ${jsonFile.absolutePath}")
    val gson = Gson()
    val tempPdfFile = File(context.cacheDir, "temp_decode.pdf")

    return try {
        val reader = FileReader(jsonFile)
        val items: Array<PdfMetadata> = gson.fromJson(reader, Array<PdfMetadata>::class.java)

        if (items.isEmpty()) {
            Log.e(TAG, "JSON parsing successful, but the metadata array is empty.")
            return null
        }

        val item = items[0]
        Log.d(TAG, "Processing metadata for Item ID: ${item.id}, Title: ${item.title}")

        // Clean the Base64 string
        val base64Data = if (item.pdfFileBase64.contains(",")) {
            Log.v(TAG, "Data URI scheme detected; stripping prefix.")
            item.pdfFileBase64.substringAfter(",")
        } else {
            item.pdfFileBase64
        }

        // Decode and Save
        Log.d(TAG, "Decoding Base64 string to temporary file...")
        saveBase64ToFile(base64Data, tempPdfFile)

        // Inject Metadata
        Log.d(TAG, "Injecting PDF metadata and saving to final cache location...")
        val finalFile = injectMetadataAndSaveToCache(context, tempPdfFile, item)

        Log.i(TAG, "Successfully processed PDF: ${finalFile.name} (${finalFile.length()} bytes)")
        finalFile

    } catch (e: Exception) {
        Log.e(TAG, "Error during JSON to PDF conversion: ${e.message}", e)
        null
    } finally {
        if (tempPdfFile.exists()) {
            val deleted = tempPdfFile.delete()
            Log.v(TAG, "Cleaned up temp file: $deleted")
        }
    }
}

private fun saveBase64ToFile(base64String: String, outputFile: File) {
    Log.d(TAG, "Starting Base64 decode. Input string length: ${base64String.length}")

    try {
        // 1. Decode the string to a byte array
        // We use NO_WRAP in case there are unexpected line breaks
        val pdfBytes = Base64.decode(base64String, Base64.DEFAULT)
        Log.v(TAG, "Successfully decoded Base64. Decoded byte array size: ${pdfBytes.size} bytes")

        // 2. Write the entire byte array at once
        FileOutputStream(outputFile).use { fos ->
            fos.write(pdfBytes)
            fos.flush() // Ensure everything is pushed to disk
        }

        Log.i(TAG, "PDF file successfully written to: ${outputFile.absolutePath}")

    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "Base64 decoding failed: Invalid characters or padding in string.", e)
        throw IOException("Invalid Base64 character detected in JSON", e)
    } catch (e: IOException) {
        Log.e(TAG, "File I/O error while saving PDF: ${e.message}", e)
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error in saveBase64ToFile: ${e.message}", e)
        throw e
    }
}

private fun injectMetadataAndSaveToCache(
    context: Context,
    inputFile: File,
    item: PdfMetadata
): File {
    val finalFile = File(context.cacheDir, "processed_${item.id}.pdf")

    try {
        PDDocument.load(inputFile).use { document ->
            val info = PDDocumentInformation().apply {
                title = item.title
                author = item.author
                producer = item.producer
                setCustomMetadataValue("OriginalID", item.id.toString())
            }
            document.documentInformation = info
            document.save(finalFile)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to inject metadata using PDFBox: ${e.message}")
        throw e
    }

    return finalFile
}