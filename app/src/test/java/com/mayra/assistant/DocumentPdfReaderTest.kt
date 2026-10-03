package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentPdfReaderTest {
    @Test fun hasTenMbLimit() { assertEquals(10L * 1024L * 1024L, DocumentPdfReader.MAX_BYTES) }
    @Test fun hasExtractionSafetyLimits() {
        assertEquals(500, DocumentPdfReader.MAX_PAGES)
        assertEquals(500_000, DocumentPdfReader.MAX_TEXT_CHARS)
    }

    @Test fun previewIsBounded() {
        val preview = DocumentPdfReader.preview("x".repeat(DocumentPdfReader.MAX_PREVIEW_CHARS + 1))
        assertTrue(preview.contains("[PDF Preview সীমিত করা হয়েছে]"))
    }
}
