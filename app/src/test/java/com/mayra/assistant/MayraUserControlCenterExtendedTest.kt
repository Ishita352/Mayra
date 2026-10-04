package com.mayra.assistant

import org.junit.Assert.assertTrue
import org.junit.Test

class MayraUserControlCenterExtendedTest {
    @Test fun includesRequestedControls() {
        val commands = MayraUserControlCenter.voiceCommands()
        assertTrue(commands.any { it.contains("voice command") })
        assertTrue(commands.any { it.contains("lock") || it.contains("lock থাকা") })
        assertTrue(commands.any { it.contains("silent") || it.contains("silent mode") })
        assertTrue(commands.any { it.contains("volume") || it.contains("ভলিউম") })
    }

    @Test fun rulesCoverSilentLockedAndVolumeBehavior() {
        assertTrue(MayraUserControlCenter.silentModeRule().contains("কথা বলবে না"))
        assertTrue(MayraUserControlCenter.lockedPhoneRule().contains("sensitive"))
        assertTrue(MayraUserControlCenter.volumeRule().contains("Owner"))
    }
}
