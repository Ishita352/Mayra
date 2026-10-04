package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraIncomeMarketIntelligenceTest {
    @Test fun prioritizesUsdFirst() {
        assertEquals(
            MayraIncomeMarketIntelligence.IncomePriority.USD_FOREIGN_CURRENCY_FIRST,
            MayraIncomeMarketIntelligence.priority()
        )
    }

    @Test fun blocksFinancialExecution() {
        val opportunity = MayraIncomeMarketIntelligence.Opportunity(
            "Example market", MayraIncomeMarketIntelligence.Area.STOCK_MARKET,
            "USD", "https://example.com", true, true
        )
        assertEquals(
            MayraIncomeMarketIntelligence.Decision.BLOCKED,
            MayraIncomeMarketIntelligence.decide(
                opportunity, MayraIncomeMarketIntelligence.Action.EXECUTE_TRADE
            )
        )
    }

    @Test fun providesMarketLearningPlan() {
        val plan = MayraIncomeMarketIntelligence.plan(
            MayraIncomeMarketIntelligence.Area.CRYPTOCURRENCY, true
        )
        assertTrue(plan.learningTopics.contains("Blockchain basics"))
        assertTrue(plan.riskChecks.isNotEmpty())
    }

    @Test fun requiresApprovalBeforeResearchAction() {
        val opportunity = MayraIncomeMarketIntelligence.Opportunity(
            "Example store", MayraIncomeMarketIntelligence.Area.ECOMMERCE,
            "USD", "https://example.com"
        )
        assertEquals(
            MayraIncomeMarketIntelligence.Decision.OWNER_APPROVAL_REQUIRED,
            MayraIncomeMarketIntelligence.decide(
                opportunity, MayraIncomeMarketIntelligence.Action.RESEARCH
            )
        )
    }
}
