package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertTrue

class MayraOwnerBiodataPortfolioTest {
    @Test
    fun cvSkillsBecomeAdvancedMayraCapabilities() {
        val skills = MayraOwnerBiodataPortfolio.skills()
        assertTrue(skills.size >= 40)
        assertTrue(skills.all { it.mayraCapability.contains("Advanced assistance") })
        assertTrue(skills.any { it.name == "Data Entry" })
        assertTrue(skills.any { it.name == "Video Editing" })
        assertTrue(skills.any { it.name == "AI Training Support" })
    }

    @Test
    fun bothCvAndNoCvOpportunityTracksExist() {
        assertTrue(MayraOwnerOpportunityFinder.find(MayraOwnerOpportunityFinder.Track.CV_FIRST).isNotEmpty())
        assertTrue(MayraOwnerOpportunityFinder.find(MayraOwnerOpportunityFinder.Track.NO_CV_MICRO_WORK).isNotEmpty())
        assertTrue(MayraOwnerBiodataPortfolio.opportunities().any { !it.cvRequired })
    }

    @Test
    fun portfolioDoesNotClaimNewCredentials() {
        assertTrue(MayraOwnerBiodataPortfolio.rule().contains("No invented qualifications"))
    }
}
