package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MayraLearningCertificateAndPlatformTest {
    @Test fun certificateNeedsEvidenceAndOwnerApproval() {
        val c = MayraLearningCertificateWorkflow.prepare("Excel", "verified project evidence")!!
        assertEquals("Gopal Basak", c.holderName)
        assertFalse(MayraLearningCertificateWorkflow.readyForCv(c))
        val approved = MayraLearningCertificateWorkflow.approve(c, true)
        assertTrue(MayraLearningCertificateWorkflow.readyForCv(approved))
    }

    @Test fun profilePlanCoversProfessionalFields() {
        val p = MayraJobPlatformProfileWorkflow.plan(MayraJobPlatformProfileWorkflow.Platform.LINKEDIN)
        assertTrue(p.fields.contains("skills"))
        assertTrue(p.fields.contains("certifications"))
        assertTrue(MayraJobPlatformProfileWorkflow.rule().contains("Owner approval"))
    }
}
