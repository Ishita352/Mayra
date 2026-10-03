package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobSkillMatchWorkflowTest {
    @Test
    fun matchesOwnerExcelSkill() {
        val c = JobWatcherWorkflow.Candidate(
            "Excel data task","source",JobIncomeWorkflow.Currency.USD,
            true,false,30,true,setOf("Excel")
        )
        val m = JobSkillMatchWorkflow.match(c)
        assertTrue(m.matchedSkills.contains("excel"))
        assertEquals(1, m.matchCount)
    }

    @Test
    fun paidOrUnverifiedCandidatesAreNotRanked() {
        val c = JobWatcherWorkflow.Candidate(
            "x","source",JobIncomeWorkflow.Currency.USD,
            false,false,30,true,setOf("Excel")
        )
        assertTrue(JobSkillMatchWorkflow.rank(listOf(c)).isEmpty())
    }

    @Test
    fun approvalRemainsRequired() {
        assertTrue(JobSkillMatchWorkflow.requiresOwnerApproval())
    }
}
