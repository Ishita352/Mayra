package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsAppVoiceCommandTest {
    @Test fun whatsappReplyCommandIsRecognized() {
        val result = VoiceCommandEngine.parse("WhatsApp reply দাও")
        assertTrue(result.recognized)
        assertEquals(VoiceCommandResult.Action.REPLY_WHATSAPP, result.action)
    }
}
