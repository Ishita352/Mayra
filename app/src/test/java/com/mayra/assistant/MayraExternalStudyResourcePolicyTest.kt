package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertEquals

class MayraExternalStudyResourcePolicyTest {
    @Test
    fun ownedNotesCanBePreparedForSale() {
        val r = MayraExternalStudyResourcePolicy.Resource(
            "Original economics notes", "Economics", "https://example.edu/notes",
            MayraExternalStudyResourcePolicy.Rights.OWNER_CREATED
        )
        assertEquals(
            MayraExternalStudyResourcePolicy.Decision.ALLOW,
            MayraExternalStudyResourcePolicy.decide(r, MayraExternalStudyResourcePolicy.Use.SELL)
        )
    }

    @Test
    fun unknownOrRestrictedMaterialIsBlocked() {
        val r = MayraExternalStudyResourcePolicy.Resource(
            "Paid notes", "Any", "https://example.com",
            MayraExternalStudyResourcePolicy.Rights.RESTRICTED
        )
        assertEquals(
            MayraExternalStudyResourcePolicy.Decision.BLOCK,
            MayraExternalStudyResourcePolicy.decide(r, MayraExternalStudyResourcePolicy.Use.SELL)
        )
    }

    @Test
    fun appliesAcrossSubjects() {
        val r = MayraExternalStudyResourcePolicy.Resource(
            "Licensed notes", "Physics", "https://example.com",
            MayraExternalStudyResourcePolicy.Rights.LICENSED
        )
        assertEquals(
            MayraExternalStudyResourcePolicy.Decision.ALLOW,
            MayraExternalStudyResourcePolicy.decide(r, MayraExternalStudyResourcePolicy.Use.SELL)
        )
    }
    @Test
    fun workflowRequiresApprovalBeforePreparation() {
        val o = MayraNoteIncomeWorkflow.Opportunity(
            "Study Platform", "https://example.com/sell", "Physics",
            MayraEducationIntelligence.AcademicLevel.UNDERGRADUATE,
            "Original Notes", true, true
        )
        assertEquals(
            MayraNoteIncomeWorkflow.Stage.REVIEW_REQUIRED,
            MayraNoteIncomeWorkflow.stage(o, false)
        )
        assertEquals(
            MayraNoteIncomeWorkflow.Stage.APPROVED,
            MayraNoteIncomeWorkflow.stage(o, true)
        )
    }

}
