package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class LocalDeviceLinkCoordinatorTest {
    private val clean = DevicePreConnectionSecurityPolicy.Risk.CLEAN

    @Test fun inviteExpiresAndCannotBeReused() {
        var now = 1_000L
        val c = LocalDeviceLinkCoordinator(clockMs = { now })
        val invite = c.createInvite("device-2", 30_000L)
        assertNotNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO), clean, true))
        assertNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO), clean, true))
        now += 31_000L
        assertNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO), clean, true))
    }

    @Test fun wrongCodeDoesNotPair() {
        val c = LocalDeviceLinkCoordinator(clockMs = { 1_000L })
        c.createInvite("device-2")
        assertNull(c.acceptInvite("device-2", "000000",
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO), clean, true))
    }

    @Test fun riskyDeviceCannotPairEvenWhenOwnerApproves() {
        val c = LocalDeviceLinkCoordinator(clockMs = { 1_000L })
        val invite = c.createInvite("device-2")
        assertNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO),
            DevicePreConnectionSecurityPolicy.Risk.MALICIOUS, true))
    }

    @Test fun ownerApprovalIsRequiredAfterCleanScan() {
        val c = LocalDeviceLinkCoordinator(clockMs = { 1_000L })
        val invite = c.createInvite("device-2")
        assertNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO), clean, false))
        assertNotNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO), clean, true))
    }

    @Test fun revokeRemovesAccess() {
        val c = LocalDeviceLinkCoordinator(clockMs = { 1_000L })
        val invite = c.createInvite("device-2")
        c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO), clean, true)
        assertTrue(c.mayUse("device-2", NetworkDeviceControlPolicy.Capability.DEVICE_INFO))
        c.revoke("device-2")
        assertFalse(c.mayUse("device-2", NetworkDeviceControlPolicy.Capability.DEVICE_INFO))
    }
}
