package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test

class MayraLockedPhoneVoiceGateTest {
    @Test fun lockedModeNeedsOwnerVerification() {
        val p = TestSharedPreferences()
        LockModePolicy.setEnabled(p, true)
        assertEquals(
            MayraLockedPhoneVoiceGate.Decision.OWNER_VERIFICATION_REQUIRED,
            MayraLockedPhoneVoiceGate.decide(p, false, true, true, "status")
        )
    }

    @Test fun sensitiveTaskBlocked() {
        val p = TestSharedPreferences()
        LockModePolicy.setEnabled(p, true)
        assertEquals(
            MayraLockedPhoneVoiceGate.Decision.BLOCK,
            MayraLockedPhoneVoiceGate.decide(p, true, true, true, "payment")
        )
    }

    @Test fun bengaliSensitiveTaskBlocked() {
        val p = TestSharedPreferences()
        LockModePolicy.setEnabled(p, true)
        assertEquals(
            MayraLockedPhoneVoiceGate.Decision.BLOCK,
            MayraLockedPhoneVoiceGate.decide(p, true, true, true, "পেমেন্ট করো")
        )
    }

    @Test fun unlockedOwnerCanUseNormalVoiceCommand() {
        val p = TestSharedPreferences()
        assertEquals(
            MayraLockedPhoneVoiceGate.Decision.ALLOW_LIMITED_VOICE,
            MayraLockedPhoneVoiceGate.decide(p, true, true, false, "সময় বলো")
        )
    }
}
