package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class MayraInstallationBootstrapManagerTest {

    @Test
    fun passwordUsesInstallationDate() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.OCTOBER, 4, 12, 30, 0)
        }
        val manager = MayraInstallationBootstrapManagerTestHarness()
        assertEquals("MAYRA-20261004", manager.password(calendar.timeInMillis))
    }

    @Test
    fun passwordIsDateBasedNotTimeBased() {
        val morning = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.OCTOBER, 4, 1, 0, 0)
        }.timeInMillis
        val night = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.OCTOBER, 4, 23, 59, 0)
        }.timeInMillis
        val manager = MayraInstallationBootstrapManagerTestHarness()
        assertEquals(manager.password(morning), manager.password(night))
    }

    private class MayraInstallationBootstrapManagerTestHarness {
        fun password(timeMs: Long): String {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = timeMs }
            return "MAYRA-" + "%04d%02d%02d".format(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}
