package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundTaskPolicyTest {
    @Test
    fun allApprovedTaskTypesAreBackgroundEligible() {
        BackgroundTaskPolicy.TaskType.values().forEach {
            assertTrue(BackgroundTaskPolicy.isBackgroundAllowed(it))
            assertFalse(BackgroundTaskPolicy.requiresOwnerApproval(it))
        }
    }

    @Test
    fun dangerousBackgroundCapabilitiesRemainLocked() {
        assertFalse(BackgroundTaskPolicy.mayBypassPlatformRules())
        assertFalse(BackgroundTaskPolicy.mayRunWhileDeviceLocked())
        assertFalse(BackgroundTaskPolicy.mayPerformFinancialAction())
        assertFalse(BackgroundTaskPolicy.maySubmitExternalWorkWithoutOwnerApproval())
        assertFalse(BackgroundTaskPolicy.mayUseStealthExecution())
    }
}
