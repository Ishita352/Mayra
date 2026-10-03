package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentTextReaderTest {
    @Test fun enforcesFiveMbLimit() { assertEquals(5L * 1024L * 1024L, DocumentTextReader.MAX_BYTES) }
    @Test fun previewIsBounded() {
        val source = "x".repeat(DocumentTextReader.MAX_PREVIEW_CHARS + 100)
        val preview = DocumentTextReader.preview(source)
        assertTrue(preview.length < source.length)
        assertTrue(preview.endsWith("[Preview সীমিত করা হয়েছে]"))
    }
    @Test fun shortTextIsReturnedUnchanged() { assertEquals("Mayra TXT test", DocumentTextReader.preview("Mayra TXT test")) }
}