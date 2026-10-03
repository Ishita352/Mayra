package com.mayra.assistant

import android.content.ContentResolver
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.IOException
import java.nio.charset.StandardCharsets

object DocumentTxtToPdfConverter {
    const val MAX_TEXT_BYTES = 5L * 1024L * 1024L

    data class Result(val success: Boolean, val message: String)

    fun convert(resolver: ContentResolver, sourceUri: Uri, text: String): Result {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        if (bytes.size.toLong() > MAX_TEXT_BYTES) {
            return Result(false, "TXT input 5 MB-এর বেশি; PDF conversion করা হয়নি।")
        }
        return try {
            val outputUri = createSiblingPdf(resolver, sourceUri)
                ?: return Result(false, "PDF output file তৈরি করার জন্য writable document location পাওয়া যায়নি।")
            val document = PdfDocument()
            try {
                val pageWidth = 595
                val pageHeight = 842
                val margin = 40f
                val lineHeight = 18f
                val linesPerPage = ((pageHeight - margin * 2) / lineHeight).toInt().coerceAtLeast(1)
                val lines = text.replace("
", "
").split("
")
                var index = 0
                var pageNumber = 1
                while (index < lines.size || (lines.isEmpty() && pageNumber == 1)) {
                    val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    val canvas = page.canvas
                    val paint = android.graphics.Paint().apply { textSize = 12f }
                    var y = margin + paint.textSize
                    var count = 0
                    while (index < lines.size && count < linesPerPage) {
                        canvas.drawText(lines[index].take(90), margin, y, paint)
                        index++
                        count++
                        y += lineHeight
                    }
                    document.finishPage(page)
                    pageNumber++
                    if (lines.isEmpty()) break
                }
                resolver.openOutputStream(outputUri, "wt")?.use { output ->
                    document.writeTo(output)
                } ?: return Result(false, "PDF output write করা যায়নি।")
            } finally {
                document.close()
            }
            Result(true, "TXT → PDF conversion সফল। PDF output তৈরি হয়েছে।")
        } catch (_: IOException) {
            Result(false, "TXT → PDF conversion-এর সময় file I/O সমস্যা হয়েছে।")
        } catch (_: SecurityException) {
            Result(false, "PDF output write permission পাওয়া যায়নি।")
        }
    }

    private fun createSiblingPdf(resolver: ContentResolver, sourceUri: Uri): Uri? {
        val parent = sourceUri.toString()
        if (parent.startsWith("content://")) {
            val documents = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(
                sourceUri,
                android.provider.DocumentsContract.getTreeDocumentId(sourceUri)
            )
            return documents
        }
        return null
    }
}
