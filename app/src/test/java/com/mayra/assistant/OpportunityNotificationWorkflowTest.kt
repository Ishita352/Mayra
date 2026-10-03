package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpportunityNotificationWorkflowTest {
    @Test
    fun preparesOwnerNotificationForSafeMatchedOpportunity() {
        val c = JobWatcherWorkflow.Candidate(
            "Excel task","source",JobIncomeWorkflow.Currency.USD,
            true,false,45,true,setOf("Excel")
        )
        val n = OpportunityNotificationWorkflow.prepare(c)
        assertNotNull(n)
        assertTrue(n!!.matchedSkillIds.contains("excel"))
        assertTrue(n.requiresOwnerApproval)
        assertTrue(n.priorityScore > 0)
    }

    @Test
    fun blocksPaidOpportunity() {
        val c = JobWatcherWorkflow.Candidate(
            "paid test","source",JobIncomeWorkflow.Currency.USD,
            true,true,30,true,setOf("Excel")
        )
        assertTrue(OpportunityNotificationWorkflow.prepare(c) == null)
    }

    @Test
    fun ownerRemainsInControl() {
        assertFalse(OpportunityNotificationWorkflow.maySubmitApplication())
        assertFalse(OpportunityNotificationWorkflow.mayContactEmployer())
        assertFalse(OpportunityNotificationWorkflow.maySpendMoney())
    }
}
