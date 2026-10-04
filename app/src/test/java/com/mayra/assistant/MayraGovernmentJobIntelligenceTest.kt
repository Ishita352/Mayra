package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MayraGovernmentJobIntelligenceTest {
    private val job = MayraGovernmentJobIntelligence.Job(
        "Example Government Recruitment", "Public Authority",
        MayraGovernmentJobIntelligence.Scope.BOTH,
        "https://example.gov/recruitment",
        eligibility = "Graduation",
        syllabus = "General Studies"
    )

    @Test
    fun validatesOfficialStyleJobRecord() {
        assertTrue(MayraGovernmentJobIntelligence.validate(job))
    }

    @Test
    fun buildsCompleteApplicationGuide() {
        val guide = MayraGovernmentJobIntelligence.prepareApplicationGuide(job)
        assertTrue(guide.documentChecklist.isNotEmpty())
        assertTrue(guide.formSteps.isNotEmpty())
        assertTrue(guide.examPreparation.isNotEmpty())
        assertTrue(guide.resultTracking.isNotEmpty())
        assertTrue(guide.finalSubmissionOwnerControlled)
    }

    @Test
    fun invalidSourceIsRejected() {
        assertEquals(
            false,
            MayraGovernmentJobIntelligence.validate(job.copy(sourceUrl = "http://example.com"))
        )
    }

    @Test
    fun safetyRuleProtectsSubmission() {
        assertTrue(MayraGovernmentJobIntelligence.safetyRule().contains("OTP"))
        assertTrue(MayraGovernmentJobIntelligence.currentSourceRule().contains("official"))
    }
}
