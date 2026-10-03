package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationSchedulePolicyTest {
    @Test
    fun importantJobEventRequiresOwnerAction() {
        val e = NotificationSchedulePolicy.Event(
            NotificationSchedulePolicy.EventType.JOB_OPPORTUNITY,
            "New job", "Verified zero-cost opportunity"
        )
        assertTrue(NotificationSchedulePolicy.mayNotify(e))
        assertTrue(NotificationSchedulePolicy.requiresOwnerAction(e))
    }

    @Test
    fun emptyNotificationIsRejected() {
        val e = NotificationSchedulePolicy.Event(
            NotificationSchedulePolicy.EventType.GENERAL, "", ""
        )
        assertFalse(NotificationSchedulePolicy.mayNotify(e))
    }

    @Test
    fun noStealthOrFinancialTrigger() {
        assertFalse(NotificationSchedulePolicy.mayRunStealthily())
        assertFalse(NotificationSchedulePolicy.mayTriggerFinancialAction())
    }
}
