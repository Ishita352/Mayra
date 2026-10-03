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
