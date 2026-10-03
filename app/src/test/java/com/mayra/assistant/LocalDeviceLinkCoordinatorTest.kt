package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class LocalDeviceLinkCoordinatorTest {
    @Test fun inviteExpiresAndCannotBeReused() {
        var now = 1_000L
        val c = LocalDeviceLinkCoordinator(clockMs = { now })
        val invite = c.createInvite("device-2", 30_000L)
        assertNotNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO)))
        assertNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO)))
        now += 31_000L
        assertNull(c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO)))
    }

    @Test fun wrongCodeDoesNotPair() {
        val c = LocalDeviceLinkCoordinator(clockMs = { 1_000L })
        c.createInvite("device-2")
        assertNull(c.acceptInvite("device-2", "000000",
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO)))
    }

    @Test fun revokeRemovesAccess() {
        val c = LocalDeviceLinkCoordinator(clockMs = { 1_000L })
        val invite = c.createInvite("device-2")
        c.acceptInvite("device-2", invite.code,
            setOf(NetworkDeviceControlPolicy.Capability.DEVICE_INFO))
        assertTrue(c.mayUse("device-2", NetworkDeviceControlPolicy.Capability.DEVICE_INFO))
        c.revoke("device-2")
        assertFalse(c.mayUse("device-2", NetworkDeviceControlPolicy.Capability.DEVICE_INFO))
    }
}
