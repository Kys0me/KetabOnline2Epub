package off.kys.ketabonline2epub.domain.model

import com.google.gson.annotations.SerializedName

data class PdfMetadata(
    val id: Int,
    val title: String,
    val author: String,
    val producer: String,
    @SerializedName("pdf_file") val pdfFileBase64: String
)