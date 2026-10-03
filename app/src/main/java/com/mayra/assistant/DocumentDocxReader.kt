package com.mayra.assistant

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object DocumentDocxReader {
    const val MAX_BYTES = 10L * 1024L * 1024L
    const val MAX_TEXT_CHARS = 500_000
    data class Result(val success: Boolean, val text: String = "", val message: String)

    fun read(resolver: ContentResolver, uri: Uri): Result {
        try {
            val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            if (size > MAX_BYTES) return Result(false, message = "DOCX 10 MB-এর বেশি; নিরাপত্তার জন্য Mayra এটি এখন পড়বে না।")
            val input = resolver.openInputStream(uri) ?: return Result(false, message = "DOCX fileটি পড়া যায়নি।")
            val raw = try {
                val out = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_BYTES) return Result(false, message = "DOCX 10 MB-এর বেশি; নিরাপত্তার জন্য Mayra এটি এখন পড়বে না।")
                    out.write(buffer, 0, count)
                }
                out.toByteArray()
            } finally {
                input.close()
            }

            ZipInputStream(raw.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val xml = readEntryBytesLimited(zip, MAX_BYTES)
                        val factory = DocumentBuilderFactory.newInstance()
                        factory.isNamespaceAware = false
                        try { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) } catch (_: Exception) {}
                        try { factory.setFeature("http://xml.org/sax/features/external-general-entities", false) } catch (_: Exception) {}
                        try { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false) } catch (_: Exception) {}
                        val parsed = factory.newDocumentBuilder().parse(xml.inputStream())
                        val paragraphs = parsed.getElementsByTagName("w:p")
                        val out = StringBuilder()
                        for (i in 0 until paragraphs.length) {
                            if (out.length >= MAX_TEXT_CHARS) break
                            val paragraph = paragraphs.item(i)
                            val textNodes = paragraph.childNodes
                            val paragraphText = StringBuilder()
                            for (j in 0 until textNodes.length) {
                                val node = textNodes.item(j)
                                if (node.nodeName == "w:r") {
                                    val runChildren = node.childNodes
                                    for (k in 0 until runChildren.length) {
                                        val child = runChildren.item(k)
                                        when (child.nodeName) {
                                            "w:t" -> paragraphText.append(child.textContent)
                                            "w:tab" -> paragraphText.append('\t')
                                            "w:br" -> paragraphText.append('\n')
                                        }
                                    }
                                }
                            }
                            if (paragraphText.isNotEmpty()) {
                                if (out.isNotEmpty()) out.append('\n')
                                out.append(paragraphText)
                            }
                        }
                        return Result(true, out.toString().trim(), "DOCX successfully read হয়েছে। Basic text extraction সম্পন্ন হয়েছে; original structure অপরিবর্তিত।")
                    }
                    entry = zip.nextEntry
                }
            }
            return Result(false, message = "DOCX document.xml পাওয়া যায়নি।")
        } catch (_: IOException) {
            return Result(false, message = "DOCX পড়ার সময় সমস্যা হয়েছে বা fileটি valid DOCX নয়।")
        } catch (_: SecurityException) {
            return Result(false, message = "DOCX file access-এর permission পাওয়া যায়নি।")
        } catch (_: Exception) {
            return Result(false, message = "DOCX processing নিরাপদভাবে সম্পন্ন করা যায়নি।")
        }
    }

    private fun readEntryBytesLimited(zip: ZipInputStream, limit: Long): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val count = zip.read(buffer)
            if (count < 0) break
            total += count
            if (total > limit) throw IOException("entry too large")
            out.write(buffer, 0, count)
        }
        return out.toByteArray()
    }
}
