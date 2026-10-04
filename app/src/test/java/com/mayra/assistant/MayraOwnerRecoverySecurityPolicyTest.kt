package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraOwnerRecoverySecurityPolicyTest {
    @Test fun correctRecoveryAnswerRequiresVoiceVerification() {
        assertEquals(
            MayraOwnerRecoverySecurityPolicy.RecoveryResult.VOICE_VERIFICATION_REQUIRED,
            MayraOwnerRecoverySecurityPolicy.afterRecoveryAnswerCorrect()
        )
        assertFalse(MayraOwnerRecoverySecurityPolicy.mayBypassVoiceAfterRecovery())
    }

    @Test fun matchingOwnerVoiceAllowsMayra() {
        assertEquals(
            MayraOwnerRecoverySecurityPolicy.RecoveryResult.OWNER_VERIFIED,
            MayraOwnerRecoverySecurityPolicy.verifyOwnerVoice(true)
        )
        assertTrue(MayraOwnerRecoverySecurityPolicy.mayUseMayraAfterRecovery(true, true))
    }

    @Test fun mismatchedVoiceLocksMayraSession() {
        assertEquals(
            MayraOwnerRecoverySecurityPolicy.RecoveryResult.MAYRA_SESSION_LOCKED,
            MayraOwnerRecoverySecurityPolicy.verifyOwnerVoice(false)
        )
        assertFalse(MayraOwnerRecoverySecurityPolicy.mayUseMayraAfterRecovery(true, false))
        assertTrue(MayraOwnerRecoverySecurityPolicy.locksMayraSessionOnVoiceMismatch())
    }
}
