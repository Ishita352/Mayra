package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeIntegrationWorkflowTest {
    @Test
    fun connectsLearningToOwnerSkillAndFutureWorkflows() {
        val i = KnowledgeIntegrationWorkflow.prepare("Excel Power Query")
        assertTrue(i.learningStages.contains(SelfLearningWorkflow.Stage.CROSS_CHECK))
        assertTrue(i.canImproveJobMatching)
        assertTrue(i.canImproveInterviewPreparation)
        assertTrue(i.requiresOwnerVerification)
    }

    @Test
    fun learningCannotAutomaticallyBecomeProvenSkill() {
        assertFalse(KnowledgeIntegrationWorkflow.mayPromoteToProvenSkill())
    }
}
