package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraOwnerVerificationPolicyTest {
    @Test fun deviceCredentialAndAndroidBiometricAreSupported() {
        assertTrue(MayraOwnerVerificationPolicy.mayVerifyWithDeviceCredential())
        assertTrue(MayraOwnerVerificationPolicy.mayVerifyWithAndroidBiometric())
    }

    @Test fun voiceAndCameraRequireConsent() {
        assertFalse(MayraOwnerVerificationPolicy.mayVerifyWithVoice(false))
        assertTrue(MayraOwnerVerificationPolicy.mayVerifyWithVoice(true))
        assertFalse(MayraOwnerVerificationPolicy.mayVerifyWithCamera(false))
        assertTrue(MayraOwnerVerificationPolicy.mayVerifyWithCamera(true))
    }

    @Test fun silentBiometricScanningIsNeverAllowed() {
        assertFalse(MayraOwnerVerificationPolicy.mayUseSilentCameraBiometricScan())
        assertFalse(MayraOwnerVerificationPolicy.mayUseSilentVoiceBiometricScan())
    }
}
