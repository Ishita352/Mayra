package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelfLearningWorkflowTest {
    @Test
    fun buildsVerifiedLearningPipeline() {
        val p = SelfLearningWorkflow.plan("Excel Power Query")
        assertTrue(p.recognized)
        assertTrue(p.stages.contains(SelfLearningWorkflow.Stage.CROSS_CHECK))
        assertTrue(p.stages.contains(SelfLearningWorkflow.Stage.OWNER_APPROVAL))
        assertTrue(p.requiresOwnerApproval)
    }

    @Test
    fun cannotSaveUnverifiedKnowledge() {
        assertFalse(SelfLearningWorkflow.maySaveUnverifiedKnowledge())
    }

    @Test
    fun cannotChangeSecurityOrFinancialLock() {
        assertFalse(SelfLearningWorkflow.mayChangeSecurityPolicy())
        assertFalse(SelfLearningWorkflow.mayChangeFinancialLock())
    }
}
