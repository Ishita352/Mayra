package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkDeviceControlPolicyTest {
    @Test fun wifiOrHotspotDoesNotEqualConsent() {
        assertFalse(NetworkDeviceControlPolicy.mayTreatNetworkPresenceAsConsent())
        assertTrue(NetworkDeviceControlPolicy.requiresExplicitPairing())
        assertFalse(NetworkDeviceControlPolicy.mayAccessWithoutPairing())
    }
    @Test fun pairedDeviceNeedsCapabilityGrant() {
        assertTrue(NetworkDeviceControlPolicy.mayControlDevice(
            NetworkDeviceControlPolicy.PairingState.PAIRED, true))
        assertFalse(NetworkDeviceControlPolicy.mayControlDevice(
            NetworkDeviceControlPolicy.PairingState.PAIRED, false))
    }
    @Test fun phoneMediaAndRecoveryRequireOwnerApproval() {
        assertTrue(NetworkDeviceControlPolicy.requiresOwnerApprovalForSensitiveCapability(
            NetworkDeviceControlPolicy.Capability.PHONE_CAMERA_FRONT))
        assertTrue(NetworkDeviceControlPolicy.requiresOwnerApprovalForSensitiveCapability(
            NetworkDeviceControlPolicy.Capability.PHONE_CAMERA_BACK))
        assertTrue(NetworkDeviceControlPolicy.requiresOwnerApprovalForSensitiveCapability(
            NetworkDeviceControlPolicy.Capability.PHONE_MICROPHONE))
        assertTrue(NetworkDeviceControlPolicy.requiresOwnerApprovalForSensitiveCapability(
            NetworkDeviceControlPolicy.Capability.PHONE_SPEAKER))
        assertTrue(NetworkDeviceControlPolicy.requiresOwnerApprovalForSensitiveCapability(
            NetworkDeviceControlPolicy.Capability.PHONE_REMOTE_RECOVERY))
    }

    @Test fun sensitiveControlCannotBypassSecurity() {
        assertTrue(NetworkDeviceControlPolicy.requiresOwnerApprovalForSensitiveCapability(
            NetworkDeviceControlPolicy.Capability.SCREEN_CONTROL))
        assertFalse(NetworkDeviceControlPolicy.mayBypassLockScreen())
        assertFalse(NetworkDeviceControlPolicy.mayBypassPasswordOrBiometrics())
        assertFalse(NetworkDeviceControlPolicy.mayExecuteArbitraryCommands())
        assertFalse(NetworkDeviceControlPolicy.mayPersistAfterRevocation())
    }
}
