package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraIncomingCallPolicyTest {
    @Test fun defaultAnswersTwoSecondsBeforeEstimatedRingEnd() {
        assertEquals(28_000L, MayraIncomingCallPolicy.answerDelayMs())
    }

    @Test fun delayNeverBecomesNegative() {
        assertEquals(0L, MayraIncomingCallPolicy.answerDelayMs(1_000L))
    }

    @Test fun autoAnswerRequiresMasterAssistantAndSecurity() {
        assertTrue(MayraIncomingCallPolicy.mayAutoAnswer(true, true, true))
        assertFalse(MayraIncomingCallPolicy.mayAutoAnswer(false, true, true))
        assertFalse(MayraIncomingCallPolicy.mayAutoAnswer(true, false, true))
        assertFalse(MayraIncomingCallPolicy.mayAutoAnswer(true, true, false))
    }

    @Test fun answersOnlyAtOrAfterConfiguredDelay() {
        assertFalse(MayraIncomingCallPolicy.shouldAnswerAt(27_999L, 0L))
        assertTrue(MayraIncomingCallPolicy.shouldAnswerAt(28_000L, 0L))
    }

    @Test fun callEndingCancelsPendingAnswer() {
        assertTrue(MayraIncomingCallPolicy.cancelsWhenCallStopsRinging())
    }
}
