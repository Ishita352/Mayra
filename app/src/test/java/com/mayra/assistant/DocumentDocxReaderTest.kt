package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertTrue

class DocumentDocxReaderTest {
    @Test fun hasTenMbLimit() { assertEquals(10L * 1024L * 1024L, DocumentDocxReader.MAX_BYTES) }

    @Test fun writerPreservesParagraphBoundariesAndEscapesXml() {
        val xml = DocumentDocxWriter.documentXml("প্রথম <লাইন>\nদ্বিতীয় & লাইন")
        assertTrue(xml.contains("<w:p><w:r><w:t xml:space=\"preserve\">প্রথম &lt;লাইন&gt;</w:t></w:r></w:p>"))
        assertTrue(xml.contains("<w:p><w:r><w:t xml:space=\"preserve\">দ্বিতীয় &amp; লাইন</w:t></w:r></w:p>"))
    }
}
