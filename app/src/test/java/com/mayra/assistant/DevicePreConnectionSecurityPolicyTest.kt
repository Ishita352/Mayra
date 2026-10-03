package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DevicePreConnectionSecurityPolicyTest {
    @Test fun scanIsRequiredBeforePairing() {
        assertTrue(DevicePreConnectionSecurityPolicy.requiresScanBeforePairing())
        assertFalse(DevicePreConnectionSecurityPolicy.mayConnectWithoutOwnerApproval(
            DevicePreConnectionSecurityPolicy.Risk.CLEAN))
    }

    @Test fun maliciousDeviceIsBlocked() {
        assertTrue(DevicePreConnectionSecurityPolicy.mustBlockAutomatically(
            DevicePreConnectionSecurityPolicy.Risk.MALICIOUS))
        assertTrue(DevicePreConnectionSecurityPolicy.mustWarnOwner(
            DevicePreConnectionSecurityPolicy.Risk.MALICIOUS))
        assertFalse(DevicePreConnectionSecurityPolicy.mayConnectAfterOwnerApproval(
            DevicePreConnectionSecurityPolicy.Risk.MALICIOUS))
    }

    @Test fun cleanDeviceStillNeedsOwnerApproval() {
        assertTrue(DevicePreConnectionSecurityPolicy.mayAskOwnerForApproval(
            DevicePreConnectionSecurityPolicy.Risk.CLEAN))
        assertTrue(DevicePreConnectionSecurityPolicy.mayConnectAfterOwnerApproval(
            DevicePreConnectionSecurityPolicy.Risk.CLEAN))
    }

    @Test fun unknownRiskFailsClosed() {
        assertTrue(DevicePreConnectionSecurityPolicy.mayAskOwnerForApproval(
            DevicePreConnectionSecurityPolicy.Risk.UNKNOWN))
        assertTrue(DevicePreConnectionSecurityPolicy.mustWarnOwner(
            DevicePreConnectionSecurityPolicy.Risk.UNKNOWN))
        assertFalse(DevicePreConnectionSecurityPolicy.mayConnectWithoutOwnerApproval(
            DevicePreConnectionSecurityPolicy.Risk.UNKNOWN))
        assertTrue(DevicePreConnectionSecurityPolicy.requiresVerifiedScanSource())
    }
}
