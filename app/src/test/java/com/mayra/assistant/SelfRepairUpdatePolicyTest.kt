package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelfRepairUpdatePolicyTest {
    @Test
    fun normalBugFixRequiresOwnerApproval() {
        val proposal = SelfRepairUpdatePolicy.assess(
            SelfRepairUpdatePolicy.ChangeArea.BUG_FIX,
            "Fix TXT to PDF text truncation"
        )
        assertTrue(proposal.requiresOwnerApproval)
        assertTrue(SelfRepairUpdatePolicy.canApply(proposal, true))
        assertFalse(SelfRepairUpdatePolicy.canApply(proposal, false))
    }

    @Test
    fun protectedSecurityAreasCannotBeSelfApplied() {
        val proposal = SelfRepairUpdatePolicy.assess(
            SelfRepairUpdatePolicy.ChangeArea.SECURITY_BOUNDARY,
            "Change owner authentication boundary"
        )
        assertTrue(proposal.requiresOwnerApproval)
        assertFalse(SelfRepairUpdatePolicy.canApply(proposal, true))
    }

    @Test
    fun readinessSeparatesCiFromPhysicalDeviceValidation() {
        val ciOnly = FinalReadinessGate.evaluate(true, true, true, true, false)
        assertTrue(ciOnly.ciReleaseReady)
        assertFalse(ciOnly.fullyDeviceValidated)

        val deviceReady = FinalReadinessGate.evaluate(true, true, true, true, true)
        assertTrue(deviceReady.fullyDeviceValidated)
    }
}
