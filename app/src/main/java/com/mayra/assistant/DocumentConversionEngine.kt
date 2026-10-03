package com.mayra.assistant

import android.content.ContentResolver
import android.net.Uri

/** Central offline-first text-based document conversion dispatcher. */
object DocumentConversionEngine {
    enum class Source { TXT, DOCX, PDF }
    enum class Target { PDF, DOCX, TXT }
    data class Result(val success: Boolean, val message: String)

    fun convertTextToPdf(resolver: ContentResolver, outputUri: Uri, text: String): Result =
        DocumentTxtToPdfConverter.write(resolver, outputUri, text).let { Result(it.success, it.message) }

    fun convertDocxToPdf(resolver: ContentResolver, outputUri: Uri, text: String): Result =
        convertTextToPdf(resolver, outputUri, text)

    fun convertPdfTextToPdf(resolver: ContentResolver, outputUri: Uri, text: String): Result =
        convertTextToPdf(resolver, outputUri, text)

    fun supports(source: Source, target: Target): Boolean = when (source to target) {
        Source.TXT to Target.PDF, Source.DOCX to Target.PDF, Source.PDF to Target.PDF -> true
        else -> false
    }
}
