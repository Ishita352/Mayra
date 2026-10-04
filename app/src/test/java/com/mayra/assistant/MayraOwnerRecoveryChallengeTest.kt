package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraOwnerRecoveryChallengeTest {
    @Test fun asksWhoCreatedMayra() {
        assertEquals("মায়রাকে কে বানিয়েছে?", MayraOwnerRecoveryChallenge.question())
    }

    @Test fun acceptsConfiguredOwnerAnswer() {
        assertTrue(MayraOwnerRecoveryChallenge.verifyAnswer("গোপাল বসাক"))
        assertTrue(MayraOwnerRecoveryChallenge.verifyAnswer("  গোপাল   বসাক "))
    }

    @Test fun rejectsWrongAnswer() {
        assertFalse(MayraOwnerRecoveryChallenge.verifyAnswer("অন্য কেউ"))
    }

    @Test fun recoveryIsOnlyFallback() {
        assertTrue(MayraOwnerRecoveryChallenge.mayOfferAfterPrimaryVerificationFailure())
    }
}
