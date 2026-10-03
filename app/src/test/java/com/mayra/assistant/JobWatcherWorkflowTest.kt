package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobWatcherWorkflowTest {
    private fun candidate(verified: Boolean, paid: Boolean) =
        JobWatcherWorkflow.Candidate("data task","source",JobIncomeWorkflow.Currency.USD,verified,paid,30,true,setOf("Excel"))

    @Test
    fun onlyFreeVerifiedCandidatesNotify() {
        val policy = JobWatcherWorkflow.ScanPolicy(true)
        assertTrue(JobWatcherWorkflow.shouldNotify(candidate(true,false), policy))
        assertFalse(JobWatcherWorkflow.shouldNotify(candidate(false,false), policy))
        assertFalse(JobWatcherWorkflow.shouldNotify(candidate(true,true), policy))
    }

    @Test
    fun rankingRemovesPaidAndUnverified() {
        val result = JobWatcherWorkflow.rank(listOf(candidate(true,false), candidate(false,false), candidate(true,true)))
        assertEquals(1, result.size)
    }

    @Test
    fun watcherCannotAutoApplyOrPay() {
        assertFalse(JobWatcherWorkflow.mayAutoApply())
        assertFalse(JobWatcherWorkflow.maySendEmployerMessage())
        assertFalse(JobWatcherWorkflow.mayMakePayment())
    }
}
