package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomeWorkRulesTest {
    @Test fun humanOnlyWithAllowedAssistanceUsesHumanAssistedMode() {
        val d = IncomeWorkRules.evaluate(true, true, true)
        assertEquals(IncomeWorkRules.WorkMode.HUMAN_ASSISTED, d.mode)
    }

    @Test fun humanOnlyWithoutAssistanceIsBlocked() {
        val d = IncomeWorkRules.evaluate(true, false, true)
        assertEquals(IncomeWorkRules.WorkMode.BLOCKED_UNVERIFIED, d.mode)
        assertFalse(IncomeWorkRules.bypassAllowed())
    }

    @Test fun unknownAutomationPermissionIsBlocked() {
        val d = IncomeWorkRules.evaluate(false, null, null)
        assertEquals(IncomeWorkRules.WorkMode.BLOCKED_UNVERIFIED, d.mode)
    }

    @Test fun explicitAutomationPermissionAllowsAutomation() {
        val d = IncomeWorkRules.evaluate(false, true, true)
        assertEquals(IncomeWorkRules.WorkMode.AUTOMATION_ALLOWED, d.mode)
    }

    @Test fun financialAndSubmissionLocksArePermanent() {
        assertFalse(IncomeWorkRules.financialActionAllowed())
        assertFalse(IncomeWorkRules.externalSubmissionAllowedWithoutOwnerApproval())
        assertFalse(MayraSkillGrowthPolicy.mayChangeSecurityOrFinancialRules())
        assertTrue(MayraSkillGrowthPolicy.learningPipeline().size >= 8)
    }
}
