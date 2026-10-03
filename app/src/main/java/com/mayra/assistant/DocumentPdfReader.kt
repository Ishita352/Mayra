package com.mayra.assistant

import android.content.ContentResolver
import android.net.Uri
import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.IOException
import java.io.Writer
import kotlin.math.min

object DocumentPdfReader {
    const val MAX_BYTES = 10L * 1024L * 1024L
    const val MAX_PREVIEW_CHARS = 8000
    const val MAX_PAGES = 500
    const val MAX_TEXT_CHARS = 500_000
    data class Result(val success: Boolean, val text: String = "", val pages: Int = 0, val message: String)

    fun read(resolver: ContentResolver, uri: Uri, context: Context): Result {
        return try {
            val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            if (size > MAX_BYTES) return Result(false, message = "PDF 10 MB-এর বেশি; নিরাপত্তার জন্য Mayra এটি এখন পড়বে না।")
            PDFBoxResourceLoader.init(context.applicationContext)
            resolver.openInputStream(uri)?.use { input ->
                val bounded = readBounded(input, MAX_BYTES)
                    ?: return Result(false, message = "PDF 10 MB-এর বেশি; নিরাপত্তার জন্য Mayra এটি এখন পড়বে না।")
                PDDocument.load(bounded.inputStream()).use { document ->
                    val pages = document.numberOfPages
                    if (pages > MAX_PAGES) {
                        return Result(false, pages = pages, message = "PDF-এ 500-এর বেশি page আছে; নিরাপত্তার জন্য Mayra এটি এখন সম্পূর্ণ read করবে না।")
                    }
                    val stripper = PDFTextStripper()
                    val writer = CappedWriter(MAX_TEXT_CHARS)
                    stripper.writeText(document, writer)
                    Result(true, writer.toString(), pages, "PDF সফলভাবে read হয়েছে। " + pages + " page পাওয়া গেছে।")
                }
            } ?: Result(false, message = "PDF fileটি পড়া যায়নি।")
        } catch (_: IOException) {
            Result(false, message = "PDF পড়ার সময় সমস্যা হয়েছে বা fileটি valid PDF নয়।")
        } catch (_: SecurityException) {
            Result(false, message = "PDF file access-এর permission পাওয়া যায়নি।")
        } catch (_: Exception) {
            Result(false, message = "PDF processing নিরাপদভাবে সম্পন্ন করা যায়নি।")
        }
    }

    private fun readBounded(input: java.io.InputStream, limit: Long): ByteArray? {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > limit) return null
            out.write(buffer, 0, count)
        }
        return out.toByteArray()
    }

    fun preview(text: String): String =
        if (text.length <= MAX_PREVIEW_CHARS) text
        else text.substring(0, MAX_PREVIEW_CHARS) + "\n\n[PDF Preview সীমিত করা হয়েছে]"

    private class CappedWriter(private val maxChars: Int) : Writer() {
        private val builder = StringBuilder()
        override fun write(cbuf: CharArray, off: Int, len: Int) {
            if (builder.length >= maxChars) return
            val allowed = min(len, maxChars - builder.length)
            builder.append(cbuf, off, allowed)
        }
        override fun flush() = Unit
        override fun close() = Unit
        override fun toString(): String = builder.toString()
    }
}
