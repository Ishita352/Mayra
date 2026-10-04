package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraWhatsAppShareTest {
    @Test fun formatsImportantMessageWithSource() {
        val message = MayraWhatsAppImportantChannel.Message(
            category = MayraWhatsAppImportantChannel.Category.JOB,
            priority = MayraWhatsAppImportantChannel.Priority.HIGH,
            title = "Remote job alert",
            body = "New matching opportunity found.",
            sourceUrl = "https://example.com/job"
        )
        val text = MayraWhatsAppShare.format(message)
        assertTrue(text.contains("Remote job alert"))
        assertTrue(text.contains("New matching opportunity found."))
        assertTrue(text.contains("https://example.com/job"))
        assertTrue(text.contains("— Mayra"))
    }

    @Test fun invalidMessageCannotBuildIntent() {
        val message = MayraWhatsAppImportantChannel.Message(
            category = MayraWhatsAppImportantChannel.Category.OTHER,
            priority = MayraWhatsAppImportantChannel.Priority.NORMAL,
            title = "",
            body = "x"
        )
        assertNull(MayraWhatsAppShare.buildIntent(message))
    }
}
