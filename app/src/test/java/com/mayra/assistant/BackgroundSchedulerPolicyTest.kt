package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundSchedulerPolicyTest {
    @Test
    fun defaultSchedulesAreOwnerVisibleAndValid() {
        assertTrue(BackgroundSchedulerPolicy.defaultSchedules().all {
            BackgroundSchedulerPolicy.isValid(it)
        })
    }

    @Test
    fun passiveIncomeEngineRunsOnTwoHourCycle() {
        val schedule = BackgroundSchedulerPolicy.defaultSchedules().first {
            it.jobType == BackgroundSchedulerPolicy.JobType.PASSIVE_INCOME_ENGINE
        }
        assertTrue(schedule.intervalHours == 2)
        assertTrue(schedule.networkRequired)
        assertTrue(schedule.ownerVisible)
        assertTrue(schedule.backgroundOnly)
    }

    @Test
    fun invalidLongIntervalIsRejected() {
        val schedule = BackgroundSchedulerPolicy.Schedule(
            BackgroundSchedulerPolicy.JobType.JOB_WATCHER, 24 * 8, true, true
        )
        assertFalse(BackgroundSchedulerPolicy.isValid(schedule))
    }

    @Test
    fun stealthAndFinancialExecutionAreDenied() {
        assertFalse(BackgroundSchedulerPolicy.mayUseStealthService())
        assertFalse(BackgroundSchedulerPolicy.mayExecuteFinancialAction())
    }
}
