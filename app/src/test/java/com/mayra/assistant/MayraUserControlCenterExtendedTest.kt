package com.mayra.assistant

import org.junit.Assert.assertTrue
import org.junit.Test

class MayraUserControlCenterExtendedTest {
    @Test fun includesRequestedControls() {
        val c=MayraUserControlCenter.voiceCommands()
        assertTrue(c.any{it.contains("voice command")}); assertTrue(c.any{it.contains("lock")})
        assertTrue(c.any{it.contains("silent")}); assertTrue(c.any{it.contains("volume")||it.contains("ভলিউম")})
    }
    @Test fun rulesCoverSilentLockedAndVolumeBehavior() {
        assertTrue(MayraUserControlCenter.silentModeRule().contains("TTS"))
        assertTrue(MayraUserControlCenter.lockedPhoneRule().contains("sensitive"))
        assertTrue(MayraUserControlCenter.volumeRule().contains("Owner"))
    }
}