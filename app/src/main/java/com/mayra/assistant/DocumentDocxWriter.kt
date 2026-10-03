package com.mayra.assistant

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocumentDocxWriter {
    const val MAX_TEXT_BYTES = 5L * 1024L * 1024L
    data class Result(val success: Boolean, val message: String)

    fun write(resolver: ContentResolver, outputUri: Uri, text: String): Result {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        if (bytes.size > MAX_TEXT_BYTES) return Result(false, "DOCX content 5 MB-এর বেশি; নিরাপত্তার জন্য সংরক্ষণ করা হয়নি।")
        try {
            val output = resolver.openOutputStream(outputUri, "w")
                ?: return Result(false, "DOCX output file খোলা যায়নি।")
            output.use { stream ->
                ZipOutputStream(stream).use { zip ->
                    put(zip, "[Content_Types].xml", contentTypes())
                    put(zip, "_rels/.rels", rootRels())
                    put(zip, "word/document.xml", documentXml(text))
                    put(zip, "word/_rels/document.xml.rels", documentRels())
                }
            }
            return Result(true, "DOCX successfully তৈরি হয়েছে। Basic text content সংরক্ষণ করা হয়েছে।")
        } catch (_: IOException) {
            return Result(false, "DOCX সংরক্ষণ করার সময় সমস্যা হয়েছে।")
        } catch (_: SecurityException) {
            return Result(false, "DOCX output file-এর permission পাওয়া যায়নি।")
        } catch (_: Exception) {
            return Result(false, "DOCX processing নিরাপদভাবে সম্পন্ন করা যায়নি।")
        }
    }

    private fun put(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }

    private fun contentTypes() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

    private fun rootRels() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    private fun documentRels() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"></Relationships>"""

    private fun documentXml(text: String): String {
        val paragraphs = text.split("\n").joinToString("") { line ->
            "<w:p><w:r><w:t xml:space=\"preserve\">\${escapeXml(line)}</w:t></w:r></w:p>"
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:body>$paragraphs</w:body>
</w:document>"""
    }

    private fun escapeXml(value: String): String = buildString(value.length) {
        for (char in value) {
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(char)
            }
        }
    }
}
