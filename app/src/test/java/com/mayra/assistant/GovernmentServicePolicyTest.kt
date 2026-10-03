package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GovernmentServicePolicyTest {
    @Test
    fun documentAndApplicationPreparationAreSupported() {
        assertTrue(GovernmentServicePolicy.mayPrepareDocuments())
        assertTrue(GovernmentServicePolicy.mayPrepareFormDraft())
        assertTrue(GovernmentServicePolicy.mayOpenOfficialApplication())
    }

    @Test
    fun sensitiveStepsRemainOwnerControlled() {
        assertFalse(GovernmentServicePolicy.mayPerformFinalSubmissionWithoutOwnerApproval())
        assertFalse(GovernmentServicePolicy.mayBypassOtpCaptchaOrIdentityVerification())
        assertFalse(GovernmentServicePolicy.mayInventEligibility())
        assertFalse(GovernmentServicePolicy.mayInventSchemeUpdate())
    }
}
