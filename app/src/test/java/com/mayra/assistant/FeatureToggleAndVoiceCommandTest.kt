package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureToggleAndVoiceCommandTest {
    @Test
    fun featureToggleDefaultsAreSafe() {
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.VOICE_COMMAND))
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.CAMERA))
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.INCOMING_CALL_ASSISTANT))
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.WHATSAPP_ASSISTANT))
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.SECURITY))
        assertFalse(FeatureToggleRegistry.defaultFor("unknown_key"))
    }

    @Test
    fun voiceOffCommandRemainsRecognized() {
        val result = VoiceCommandEngine.parse("মায়রা, ভয়েস কমান্ড বন্ধ করো")
        assertTrue(result.recognized)
        assertEquals(VoiceCommandResult.Action.SET_VOICE_COMMAND, result.action)
    }

    @Test
    fun cameraToggleCommandsAreDistinguishedFromOpeningCamera() {
        val off = VoiceCommandEngine.parse("মায়রা, ক্যামেরা বন্ধ করো")
        assertEquals(VoiceCommandResult.Action.SET_CAMERA, off.action)

        val open = VoiceCommandEngine.parse("মায়রা ক্যামেরা খোলো")
        assertEquals(VoiceCommandResult.Action.OPEN_CAMERA, open.action)
    }

    @Test
    fun incomingCallAssistantDefaultsToOff() {
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.INCOMING_CALL_ASSISTANT))
        val result = VoiceCommandEngine.parse("মায়রা, ইনকামিং কল অ্যাসিস্ট্যান্ট চালু করো")
        assertEquals(VoiceCommandResult.Action.SET_INCOMING_CALL_ASSISTANT, result.action)
    }
}
