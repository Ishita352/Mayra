package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomePlatformEligibilityPolicyTest {
    @Test fun unknownPolicyIsRestricted() {
        assertEquals(
            IncomePlatformEligibilityPolicy.PlatformAccess.UNKNOWN_RESTRICTED,
            IncomePlatformEligibilityPolicy.classify(false, false, false)
        )
    }

    @Test fun aiBannedAndHumanOnlyAreSeparated() {
        assertTrue(
            IncomePlatformEligibilityPolicy.shouldSeparateFromAiAssistedOpportunities(
                IncomePlatformEligibilityPolicy.PlatformAccess.AI_BANNED
            )
        )
        assertTrue(
            IncomePlatformEligibilityPolicy.shouldSeparateFromAiAssistedOpportunities(
                IncomePlatformEligibilityPolicy.PlatformAccess.HUMAN_ONLY
            )
        )
    }

    @Test fun allowedPlatformCanUsePermittedAssistance() {
        assertTrue(
            IncomePlatformEligibilityPolicy.mayGenerateSubmissionContent(
                IncomePlatformEligibilityPolicy.PlatformAccess.AI_ALLOWED
            )
        )
        assertFalse(IncomePlatformEligibilityPolicy.mayBypassRestriction(
            IncomePlatformEligibilityPolicy.PlatformAccess.AI_BANNED
        ))
    }
}
