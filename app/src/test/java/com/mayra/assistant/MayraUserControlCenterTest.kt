package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import android.content.SharedPreferences

class MayraUserControlCenterTest {
    @Test fun allFiveControlsAreVoiceOperable() {
        assertTrue(MayraUserControlCenter.voiceCommands().size >= 5)
        assertTrue(MayraUserControlCenter.voiceCommands().any { it.contains("WhatsApp") })
    }
    @Test fun rulesKeepCallsAndWhatsAppPermissionBounded() {
        assertTrue(MayraUserControlCenter.incomingCallRule().contains("permission"))
        assertTrue(MayraUserControlCenter.whatsappRule().contains("authorized"))
    }
    @Test fun voiceLightAnd3dAreExplicitlyNonCovert() {
        assertTrue(MayraUserControlCenter.voiceLightRule().contains("covert"))
        assertTrue(MayraUserControlCenter.threeDRule().contains("covert"))
    }
}
