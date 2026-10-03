package com.mayra.assistant

import android.content.ContentResolver
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.IOException
import java.nio.charset.StandardCharsets

object DocumentTxtToPdfConverter {
    const val MAX_TEXT_BYTES = 5L * 1024L * 1024L
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 18f
    private const val TEXT_SIZE = 12f

    data class Result(val success: Boolean, val message: String)

    fun write(resolver: ContentResolver, outputUri: Uri, text: String): Result {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        if (bytes.size.toLong() > MAX_TEXT_BYTES) {
            return Result(false, "TXT input 5 MB-এর বেশি; PDF conversion করা হয়নি।")
        }
        return try {
            val document = PdfDocument()
            try {
                val paint = android.graphics.Paint().apply { textSize = TEXT_SIZE }
                val linesPerPage =
                    ((PAGE_HEIGHT - MARGIN * 2) / LINE_HEIGHT).toInt().coerceAtLeast(1)
                val lines = text.replace("\r\n", "\n").replace("\r", "\n").split("\n")
                    .flatMap { wrapLine(it, paint) }
                var index = 0
                var pageNumber = 1
                while (index < lines.size || lines.isEmpty()) {
                    val page = document.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    )
                    val canvas = page.canvas
                    var y = MARGIN + paint.textSize
                    var count = 0
                    while (index < lines.size && count < linesPerPage) {
                        canvas.drawText(lines[index], MARGIN, y, paint)
                        index++
                        count++
                        y += LINE_HEIGHT
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

    private fun wrapLine(
        line: String,
        paint: android.graphics.Paint
    ): List<String> {
        if (line.isEmpty()) return listOf("")
        val maxWidth = PAGE_WIDTH - MARGIN * 2
        val result = mutableListOf<String>()
        var start = 0
        while (start < line.length) {
            var end = start + 1
            while (end <= line.length && paint.measureText(line, start, end) <= maxWidth) {
                end++
            }
            val hardEnd = (end - 1).coerceAtLeast(start + 1).coerceAtMost(line.length)
            var breakAt = hardEnd
            if (hardEnd < line.length) {
                val whitespace = line.lastIndexOfAny(charArrayOf(' ', '\t'), hardEnd - 1)
                if (whitespace > start) breakAt = whitespace
            }
            result += line.substring(start, breakAt).trimEnd()
            start = if (breakAt < line.length && line[breakAt].isWhitespace()) breakAt + 1 else breakAt
        }
        return result
    }
}
