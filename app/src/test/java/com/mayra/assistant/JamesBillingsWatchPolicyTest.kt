package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JamesBillingsWatchPolicyTest {
    @Test fun defaultIsHighAlertAndOwnerVisible() {
        val c = JamesBillingsWatchPolicy.defaultCheck()
        assertEquals(12, c.intervalHours)
        assertTrue(c.networkRequired)
        assertTrue(c.ownerVisible)
        assertTrue(c.highAlert)
    }

    @Test fun changedFingerprintTriggersNotification() {
        assertTrue(JamesBillingsWatchPolicy.shouldNotify("old", "new"))
        assertFalse(JamesBillingsWatchPolicy.shouldNotify("same", "same"))
        assertFalse(JamesBillingsWatchPolicy.shouldNotify("old", null))
    }

    @Test fun unsafeActionsRemainDenied() {
        assertFalse(JamesBillingsWatchPolicy.mayUseStealthService())
        assertFalse(JamesBillingsWatchPolicy.mayReadPrivateAccountData())
        assertFalse(JamesBillingsWatchPolicy.mayAutoApply())
        assertFalse(JamesBillingsWatchPolicy.mayExecuteFinancialAction())
    }
}