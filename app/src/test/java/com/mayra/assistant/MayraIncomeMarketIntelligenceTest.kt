package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraIncomeMarketIntelligenceTest {
    @Test fun prioritizesUsdFirst() = assertEquals(MayraIncomeMarketIntelligence.IncomePriority.USD_FOREIGN_CURRENCY_FIRST,MayraIncomeMarketIntelligence.priority())
    @Test fun blocksFinancialExecution() {
        val o=MayraIncomeMarketIntelligence.Opportunity("Example",MayraIncomeMarketIntelligence.Area.STOCK_MARKET,"USD","https://example.com",true,true)
        assertEquals(MayraIncomeMarketIntelligence.Decision.BLOCKED,MayraIncomeMarketIntelligence.decide(o,MayraIncomeMarketIntelligence.Action.EXECUTE_TRADE))
    }
    @Test fun providesMarketLearningPlan() {
        val p=MayraIncomeMarketIntelligence.plan(MayraIncomeMarketIntelligence.Area.CRYPTOCURRENCY,true)
        assertTrue(p.learningTopics.contains("Blockchain basics"))
    }
    @Test fun requiresApprovalBeforeResearchAction() {
        val o=MayraIncomeMarketIntelligence.Opportunity("Example",MayraIncomeMarketIntelligence.Area.ECOMMERCE,"USD","https://example.com",true)
        assertEquals(MayraIncomeMarketIntelligence.Decision.OWNER_APPROVAL_REQUIRED,MayraIncomeMarketIntelligence.decide(o,MayraIncomeMarketIntelligence.Action.RESEARCH))
    }
}