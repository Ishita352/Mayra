package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraVideoIncomeIntelligenceTest {
    @Test
    fun coversVerifiedVideoIncomePlatforms() {
        val names = MayraVideoIncomeIntelligence.knownPlatforms().map { it.name }
        assertTrue(names.contains("YouTube"))
        assertTrue(names.contains("Adobe Stock"))
        assertTrue(names.contains("Shutterstock Contributor"))
    }

    @Test
    fun preparesStockVideoIncomeWorkflow() {
        val platform = MayraVideoIncomeIntelligence.knownPlatforms().first { it.name == "Adobe Stock" }
        val plan = MayraVideoIncomeIntelligence.plan(platform)
        assertTrue(plan.earningRoutes.any { it.contains("original/licensed footage") })
        assertTrue(plan.legal.any { it.contains("rights") })
        assertEquals(
            MayraVideoIncomeIntelligence.Decision.OWNER_APPROVAL_REQUIRED,
            plan.decision
        )
    }

    @Test
    fun financialTransactionsAreAlwaysBlocked() {
        val platform = MayraVideoIncomeIntelligence.knownPlatforms().first()
        assertEquals(
            MayraVideoIncomeIntelligence.Decision.BLOCKED,
            MayraVideoIncomeIntelligence.decide(
                platform, MayraVideoIncomeIntelligence.Action.FINANCIAL_TRANSACTION
            )
        )
    }
}
