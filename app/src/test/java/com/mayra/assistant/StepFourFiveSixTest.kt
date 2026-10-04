package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class StepFourFiveSixTest {
    @Test fun incomeWorkflowBlocksFinancialAutomation() {
        val opportunity = MayraIncomeWorkflow.Opportunity("1","Freelance Kotlin","Example",MayraIncomeWorkflow.Type.FREELANCE,false)
        val registered = MayraIncomeWorkflow.register(opportunity)
        assertEquals(MayraIncomeWorkflow.Status.MANUAL_ONLY, registered.status)
        assertFalse(MayraIncomeWorkflow.canAutomate(registered))
        assertTrue(MayraIncomeWorkflow.earningsInstruction().contains("payment"))
    }

    @Test fun incomeWorkflowRequiresOwnerApproval() {
        val plan = MayraIncomeWorkflow.plan("freelance", listOf(
            MayraIncomeWorkflow.Opportunity("1","Freelance Kotlin","Example",MayraIncomeWorkflow.Type.FREELANCE,true)
        ))
        assertTrue(plan.requiresOwnerApproval)
    }

    @Test fun interviewSessionIsGroundedAndNonImpersonating() {
        val session = MayraInterviewAssistant.start("Android Developer")
        assertEquals("Android Developer", session.role)
        assertTrue(session.questions.isNotEmpty())
        assertTrue(session.checklist.any { it.contains("impersonation") })
    }

    @Test fun interviewAnswerEvaluationWorks() {
        assertTrue(MayraInterviewAssistant.evaluateAnswer("q", "I solved a real project problem by testing the root cause and then delivered the fix successfully.").isNotBlank())
    }

    @Test fun learningRequiresEvidenceForMastery() {
        var item = MayraLearningEngine.KnowledgeItem("1","Kotlin","claim", listOf("a","b"), skill="Kotlin")
        item = MayraLearningEngine.advance(item)
        assertEquals(MayraLearningEngine.Status.CROSS_CHECKED, item.status)
        item = MayraLearningEngine.advance(item, testPassed=true)
        assertEquals(MayraLearningEngine.Status.TESTED, item.status)
        assertNull(MayraLearningEngine.mastery(item))
        item = item.copy(evidence="Unit tests passed")
        item = MayraLearningEngine.advance(item)
        assertEquals(MayraLearningEngine.Status.VERIFIED, item.status)
        assertEquals(4, MayraLearningEngine.mastery(item)?.level)
    }
}
