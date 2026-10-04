package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraCopyrightOriginalityWorkflowTest {
    @Test
    fun copyrightedThirdPartyWorkCannotSimplyBecomeCopyrightFree() {
        val source = MayraCopyrightOriginalityWorkflow.Source(
            "Third-party video", "https://example.com/video",
            MayraCopyrightOriginalityWorkflow.SourceStatus.THIRD_PARTY_COPYRIGHTED
        )
        assertEquals(
            MayraCopyrightOriginalityWorkflow.Decision.MANUAL_REVIEW,
            MayraCopyrightOriginalityWorkflow.decide(
                source, MayraCopyrightOriginalityWorkflow.Action.PREPARE_FOR_SALE
            )
        )
        assertTrue(MayraCopyrightOriginalityWorkflow.noMagicNonCopyright())
    }

    @Test
    fun ownerCreatedWorkCanBePreparedAsOriginal() {
        val source = MayraCopyrightOriginalityWorkflow.Source(
            "Owner footage", "https://example.com/owner",
            MayraCopyrightOriginalityWorkflow.SourceStatus.OWNER_CREATED
        )
        assertEquals(
            MayraCopyrightOriginalityWorkflow.Decision.ALLOW,
            MayraCopyrightOriginalityWorkflow.decide(
                source, MayraCopyrightOriginalityWorkflow.Action.PREPARE_FOR_SALE
            )
        )
    }

    @Test
    fun uncertainRightsAreBlocked() {
        val source = MayraCopyrightOriginalityWorkflow.Source(
            "Unknown material", "https://example.com/unknown",
            MayraCopyrightOriginalityWorkflow.SourceStatus.UNKNOWN
        )
        assertEquals(
            MayraCopyrightOriginalityWorkflow.Decision.BLOCK,
            MayraCopyrightOriginalityWorkflow.decide(
                source, MayraCopyrightOriginalityWorkflow.Action.PREPARE_FOR_SALE
            )
        )
    }
}
