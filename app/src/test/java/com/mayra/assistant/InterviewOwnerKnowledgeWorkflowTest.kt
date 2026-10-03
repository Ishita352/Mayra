package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterviewOwnerKnowledgeWorkflowTest {
    @Test
    fun groundsExcelInterviewPreparationInOwnerSkill() {
        val p = InterviewOwnerKnowledgeWorkflow.prepare("Excel interview question practice")
        assertTrue(p.relevantSkillIds.contains("excel"))
        assertTrue(p.groundingStatus == "GROUNDED_IN_OWNER_SKILL_KNOWLEDGE")
    }

    @Test
    fun doesNotInventExperienceOrQualifications() {
        assertFalse(InterviewOwnerKnowledgeWorkflow.mayInventExperience())
        assertFalse(InterviewOwnerKnowledgeWorkflow.mayInventQualification())
    }
}
