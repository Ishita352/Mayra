package com.mayra.assistant

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

/** Read-only TXT reader with a hard byte limit. */
object DocumentTextReader {
    const val MAX_BYTES = 5L * 1024L * 1024L
    const val MAX_PREVIEW_CHARS = 4000

    data class Result(val success: Boolean, val text: String = "", val message: String)

    fun read(resolver: ContentResolver, uri: Uri): Result {
        return try {
            val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            if (size > MAX_BYTES) return Result(false, message = "TXT fileটি 5 MB-এর বেশি; নিরাপত্তার জন্য Mayra এটি এখন পড়বে না।")
            val bytes = resolver.openInputStream(uri)?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > MAX_BYTES) return@use null
                    output.write(buffer, 0, read)
                }
                output.toByteArray()
            } ?: return Result(false, message = "TXT fileটি পড়া যায়নি বা file access পাওয়া যায়নি।")
            if (bytes.size.toLong() > MAX_BYTES) return Result(false, message = "TXT fileটি 5 MB-এর বেশি; নিরাপত্তার জন্য Mayra এটি এখন পড়বে না।")
            val decoder = Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            Result(true, text = decoder.decode(ByteBuffer.wrap(bytes)).toString(), message = "TXT file সফলভাবে read হয়েছে।")
        } catch (_: CharacterCodingException) {
            Result(false, message = "TXT fileটি valid UTF-8 text নয়; Mayra এটি এখন নিরাপদভাবে পড়বে না।")
        } catch (_: IOException) {
            Result(false, message = "TXT file পড়ার সময় সমস্যা হয়েছে।")
        } catch (_: SecurityException) {
            Result(false, message = "TXT file access-এর permission পাওয়া যায়নি।")
        }
    }

    fun preview(text: String): String = if (text.length <= MAX_PREVIEW_CHARS) text
        else text.substring(0, MAX_PREVIEW_CHARS) + "\n\n[Preview সীমিত করা হয়েছে]"
}