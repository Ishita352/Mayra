package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentTextEditorTest {
    @Test fun outputLimitIsFiveMb() {
        assertEquals(5L * 1024L * 1024L, DocumentTextEditor.MAX_BYTES)
    }
}
