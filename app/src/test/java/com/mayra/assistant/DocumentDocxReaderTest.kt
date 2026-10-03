package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentDocxReaderTest {
    @Test fun hasTenMbLimit() { assertEquals(10L * 1024L * 1024L, DocumentDocxReader.MAX_BYTES) }
}
