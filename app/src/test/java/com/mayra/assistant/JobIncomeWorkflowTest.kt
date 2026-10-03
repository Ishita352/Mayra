package com.mayra.assistant

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class JobIncomeWorkflowTest {
    @Test
    fun rejectsUnverifiedOrPaidOpportunities() {
        val paid = JobIncomeWorkflow.Opportunity("x","y",JobIncomeWorkflow.Currency.USD,JobIncomeWorkflow.WorkType.HOURLY,30,true,true,true)
        val unverified = paid.copy(upfrontCost=false, verified=false)
        assertEquals(Int.MIN_VALUE, JobIncomeWorkflow.priority(paid))
        assertEquals(Int.MIN_VALUE, JobIncomeWorkflow.priority(unverified))
    }

    @Test
    fun prioritizesEligibleUsdRepeatableShortWork() {
        val opportunity = JobIncomeWorkflow.Opportunity("x","y",JobIncomeWorkflow.Currency.USD,JobIncomeWorkflow.WorkType.HOURLY,30,true,false,true)
        assertTrue(JobIncomeWorkflow.priority(opportunity) > 0)
    }

    @Test
    fun financialExecutionIsAlwaysDenied() {
        assertTrue(!JobIncomeWorkflow.mayraMayExecuteFinancialAction())
        assertTrue(JobIncomeWorkflow.requiresOwnerApproval())
    }
}
