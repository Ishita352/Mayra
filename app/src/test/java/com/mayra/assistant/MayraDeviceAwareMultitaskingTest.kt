package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MayraDeviceAwareMultitaskingTest {
    @Test fun capacityAdaptsToDeviceHealthAndResources() {
        val good=MayraDeviceAwareMultitasking.capacity(
            MayraDeviceAwareMultitasking.Health.GOOD,
            MayraDeviceAwareMultitasking.Load.MODERATE,70,70,70,true)
        val caution=MayraDeviceAwareMultitasking.capacity(
            MayraDeviceAwareMultitasking.Health.CAUTION,
            MayraDeviceAwareMultitasking.Load.HEAVY,25,25,20,true)
        assertTrue(good.recommendedConcurrentTasks >= caution.recommendedConcurrentTasks)
    }
    @Test fun unsafeThermalStateForcesProtection() {
        val c=MayraDeviceAwareMultitasking.capacity(
            MayraDeviceAwareMultitasking.Health.EXCELLENT,
            MayraDeviceAwareMultitasking.Load.LIGHT,90,90,90,false)
        assertEquals(1,c.recommendedConcurrentTasks)
    }
    @Test fun temporaryIncreaseNeedsOwnerApprovalAndHealthyDevice() {
        assertTrue(MayraDeviceAwareMultitasking.ownerPermissionForTemporaryLoadIncrease(1,true,true,true,true))
        assertFalse(MayraDeviceAwareMultitasking.ownerPermissionForTemporaryLoadIncrease(1,false,true,true,true))
        assertFalse(MayraDeviceAwareMultitasking.ownerPermissionForTemporaryLoadIncrease(1,true,false,true,true))
    }
}
