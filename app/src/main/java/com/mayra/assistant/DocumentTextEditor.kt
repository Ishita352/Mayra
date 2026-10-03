package com.mayra.assistant

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import java.nio.charset.StandardCharsets

object DocumentTextEditor {
    const val MAX_BYTES = 5L * 1024L * 1024L

    fun save(resolver: ContentResolver, uri: Uri, text: String): Result {
        return try {
            val bytes = text.toByteArray(StandardCharsets.UTF_8)
            if (bytes.size.toLong() > MAX_BYTES) {
                Result(false, "TXT output 5 MB-এর বেশি; নিরাপত্তার জন্য save করা হয়নি।")
            } else {
                resolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
                    ?: return Result(false, "Output file write করা যায়নি।")
                Result(true, "TXT document successfully saved হয়েছে।")
            }
        } catch (_: IOException) {
            Result(false, "TXT document save করার সময় সমস্যা হয়েছে।")
        } catch (_: SecurityException) {
            Result(false, "TXT document write permission পাওয়া যায়নি।")
        }
    }

    data class Result(val success: Boolean, val message: String)
}
