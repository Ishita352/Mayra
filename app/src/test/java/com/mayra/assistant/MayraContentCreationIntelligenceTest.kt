package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraContentCreationIntelligenceTest {
    @Test
    fun coversCreativeWorkAreas() {
        assertTrue(MayraContentCreationIntelligence.plan(
            MayraContentCreationIntelligence.Area.VIDEO_EDITING
        ).skills.contains("Audio cleanup and sync"))
        assertTrue(MayraContentCreationIntelligence.plan(
            MayraContentCreationIntelligence.Area.PHOTO_EDITING
        ).skills.contains("Retouching basics"))
        assertTrue(MayraContentCreationIntelligence.plan(
            MayraContentCreationIntelligence.Area.SOCIAL_MEDIA_MANAGEMENT
        ).skills.contains("Content calendar"))
        assertTrue(MayraContentCreationIntelligence.plan(
            MayraContentCreationIntelligence.Area.YOUTUBE
        ).deliverables.contains("Upload checklist"))
    }

    @Test
    fun publishingRequiresOwnerApproval() {
        val opportunity = MayraContentCreationIntelligence.WorkOpportunity(
            "YouTube editing marketplace", "https://example.com/jobs",
            MayraContentCreationIntelligence.Area.VIDEO_EDITING, true, false
        )
        assertEquals(
            MayraContentCreationIntelligence.Decision.OWNER_APPROVAL_REQUIRED,
            MayraContentCreationIntelligence.decide(
                opportunity, MayraContentCreationIntelligence.Action.PUBLISH
            )
        )
    }

    @Test
    fun existingExperienceIsReusableWithoutInventingNewExperience() {
        val profile = MayraContentCreationIntelligence.ownerExperienceProfile()
        assertTrue(profile.existingSkills.contains("YouTube video editing"))
        assertTrue(profile.existingSkills.contains("Photo editing/digital content work"))
        assertTrue(MayraContentCreationIntelligence.clientWorkRule().contains("must not falsely claim skills"))
    }
}
