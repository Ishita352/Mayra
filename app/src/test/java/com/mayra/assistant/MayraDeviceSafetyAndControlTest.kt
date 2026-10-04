package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraDeviceSafetyAndControlTest {
    private fun state(permission: Boolean = true, healthy: Boolean = true) =
        MayraDeviceSafetyAndControl.DeviceState(
            device = MayraDeviceSafetyAndControl.Device.ANDROID,
            ownerVerified = true,
            paired = true,
            osPermissionGranted = permission,
            storageHealthy = healthy,
            batteryHealthy = healthy,
            thermalHealthy = healthy
        )

    @Test fun requiresOwnerAndPairing() {
        val noOwner = state().copy(ownerVerified = false)
        val unpaired = state().copy(paired = false)
        assertFalse(MayraDeviceSafetyAndControl.assess(noOwner, MayraDeviceSafetyAndControl.Action.OPEN_APP).allowed)
        assertFalse(MayraDeviceSafetyAndControl.assess(unpaired, MayraDeviceSafetyAndControl.Action.OPEN_APP).allowed)
    }

    @Test fun financialAndSecurityControlsAreAlwaysBlocked() {
        assertFalse(MayraDeviceSafetyAndControl.assess(state(), MayraDeviceSafetyAndControl.Action.FINANCIAL_TRANSACTION).allowed)
        assertFalse(MayraDeviceSafetyAndControl.assess(state(), MayraDeviceSafetyAndControl.Action.SECURITY_CONTROL).allowed)
    }

    @Test fun missingOsPermissionBlocksControl() {
        val d = MayraDeviceSafetyAndControl.assess(
            state(permission = false),
            MayraDeviceSafetyAndControl.Action.FILE_ACCESS
        )
        assertFalse(d.allowed)
    }

    @Test fun unhealthyDeviceBlocksRiskyOperation() {
        val d = MayraDeviceSafetyAndControl.assess(
            state(healthy = false),
            MayraDeviceSafetyAndControl.Action.INSTALL_OR_UPDATE
        )
        assertFalse(d.allowed)
        assertEquals(MayraDeviceSafetyAndControl.Risk.CAUTION, d.risk)
    }

    @Test fun highRiskActionsRemainOwnerAuthorized() {
        val d = MayraDeviceSafetyAndControl.assess(
            state(),
            MayraDeviceSafetyAndControl.Action.SYSTEM_ADMIN
        )
        assertTrue(d.allowed)
        assertEquals(MayraDeviceSafetyAndControl.Risk.HIGH_RISK, d.risk)
        assertTrue(MayraDeviceSafetyAndControl.fullControlRule().contains("OS security"))
    }
}
