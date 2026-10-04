package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MayraPrivacyProtectionTest {
    @Test fun trackingIsOffByDefault() {
        assertFalse(MayraPrivacyProtection.telemetryEnabledByDefault())
        assertFalse(MayraPrivacyProtection.thirdPartyTrackingEnabledByDefault())
        assertFalse(MayraPrivacyProtection.persistentTrackingIdentifiersAllowed())
        assertFalse(MayraPrivacyProtection.unnecessaryTelemetryRetentionAllowed())
    }

    @Test fun rawSensitiveDataIsNotStored() {
        assertFalse(MayraPrivacyProtection.rawBiometricStorageAllowed())
        assertFalse(MayraPrivacyProtection.secretStorageAllowed())
    }

    @Test fun sensitiveSensorsDefaultToDeny() {
        assertTrue(MayraPrivacyProtection.decision(MayraPrivacyProtection.DataType.CAMERA) == MayraPrivacyProtection.Decision.DENY_BY_DEFAULT)
        assertTrue(MayraPrivacyProtection.decision(MayraPrivacyProtection.DataType.LOCATION) == MayraPrivacyProtection.Decision.DENY_BY_DEFAULT)
        assertTrue(MayraPrivacyProtection.sensitiveAccessRequiresConsent())
    }

    @Test fun remoteAccessIsOwnerApprovedAndCovertEvasionIsBlocked() {
        assertTrue(MayraPrivacyProtection.remoteAccessRequiresOwnerApproval())
        assertFalse(MayraPrivacyProtection.covertEvasionAllowed())
    }
}
