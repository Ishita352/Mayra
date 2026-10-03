package com.mayra.assistant

import android.content.ContentResolver
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.IOException

object DocumentPdfReader {
    const val MAX_BYTES = 10L * 1024L * 1024L
    const val MAX_PREVIEW_CHARS = 8000
    data class Result(val success: Boolean, val text: String = "", val pages: Int = 0, val message: String)
    fun read(resolver: ContentResolver, uri: Uri, context: android.content.Context): Result {\n        return try {
        val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        if (size > MAX_BYTES) return Result(false, message = "PDF 10 MB-এর বেশি; নিরাপত্তার জন্য Mayra এটি এখন পড়বে না।")
        PDFBoxResourceLoader.init(context.applicationContext)
        resolver.openInputStream(uri)?.use { input -> PDDocument.load(input).use { document ->
            val text = PDFTextStripper().getText(document)
            Result(true, text, document.numberOfPages, "PDF সফলভাবে read হয়েছে। " + document.numberOfPages + " page পাওয়া গেছে।")
        }} ?: Result(false, message = "PDF fileটি পড়া যায়নি।")
    } catch (_: IOException) { Result(false, message = "PDF পড়ার সময় সমস্যা হয়েছে বা fileটি valid PDF নয়।") }
      catch (_: SecurityException) { Result(false, message = "PDF file access-এর permission পাওয়া যায়নি।") }
      catch (_: Exception) { Result(false, message = "PDF processing নিরাপদভাবে সম্পন্ন করা যায়নি।") }
    fun preview(text: String): String = if (text.length <= MAX_PREVIEW_CHARS) text else text.substring(0, MAX_PREVIEW_CHARS) + "\n\n[PDF Preview সীমিত করা হয়েছে]"
}