package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraCoreOperatingPrinciplesTest {
    @Test
    fun hardFinancialActionsAreAlwaysBlocked() {
        assertEquals(MayraCoreOperatingPrinciples.Decision.BLOCKED,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.PURCHASE))
        assertEquals(MayraCoreOperatingPrinciples.Decision.BLOCKED,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.SUBSCRIBE))
        assertEquals(MayraCoreOperatingPrinciples.Decision.BLOCKED,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.PAID_UPGRADE))
        assertEquals(MayraCoreOperatingPrinciples.Decision.BLOCKED,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.BANK_TRANSACTION))
        assertTrue(MayraCoreOperatingPrinciples.hardFinancialLock())
    }

    @Test
    fun freeBuildingAndLearningAreAllowed() {
        assertEquals(MayraCoreOperatingPrinciples.Decision.ALLOW,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.CODE))
        assertEquals(MayraCoreOperatingPrinciples.Decision.ALLOW,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.BUILD_WEBSITE))
        assertEquals(MayraCoreOperatingPrinciples.Decision.ALLOW,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.BUILD_AI_ASSISTANT))
        assertTrue(MayraCoreOperatingPrinciples.freeFirstRule().contains("YouTube"))
    }

    @Test
    fun humanOnlySitesBecomeManualOnly() {
        assertEquals(MayraCoreOperatingPrinciples.Decision.MANUAL_ONLY,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.AUTOMATE_HUMAN_ONLY_SITE))
        assertEquals(MayraCoreOperatingPrinciples.Decision.ALLOW,
            MayraCoreOperatingPrinciples.decide(MayraCoreOperatingPrinciples.Action.GUIDE_OWNER_MANUALLY))
        assertTrue(MayraCoreOperatingPrinciples.examInterviewRule().contains("impersonate"))
    }
}
