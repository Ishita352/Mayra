package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StepFiveSixSelfRepairIntegrationTest {
    @Test
    fun coreRoutesSelfRepairToOwnerGatedWorkflow() {
        val answer = CoreKnowledgeEngine.answer("Mayra নিজেকে আপডেট করো, সমস্যা ঠিক করো")
        assertEquals(CoreKnowledgeEngine.Domain.SELF_REPAIR, answer.domain)
        assertTrue(answer.recognized)
        assertTrue(answer.message.contains("Owner approval"))
    }

    @Test
    fun protectedBoundariesRemainNonSelfModifiable() {
        val proposal = SelfRepairUpdatePolicy.assess(
            SelfRepairUpdatePolicy.ChangeArea.AUTHENTICATION,
            "Change owner authentication"
        )
        assertEquals(SelfRepairUpdatePolicy.Risk.BLOCKED, proposal.risk)
        assertTrue(proposal.validationSteps.isNotEmpty())
    }
}
