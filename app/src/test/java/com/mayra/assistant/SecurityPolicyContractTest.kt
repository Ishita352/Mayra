package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityPolicyContractTest {
    @Test
    fun financialLockIsAbsoluteForAnyPackage() {
        val candidates = listOf(
            null,
            "",
            "com.example.unknown",
            "com.google.android.apps.nbu.paisa.user",
            "com.phonepe.app",
            "net.one97.paytm"
        )

        candidates.forEach { packageName ->
            assertFalse(PaymentSafetyPolicy.mayraMayAutomatePackage(packageName))
        }
        assertTrue(PaymentSafetyPolicy.financialLockEnabled())
    }

    @Test
    fun unknownFeatureTogglesFailClosed() {
        val prefs = FakeSharedPreferences()
        assertFalse(FeatureToggleRegistry.isEnabled(prefs, "future_unknown_feature"))
    }

    @Test
    fun incomingCallAssistantIsOptIn() {
        val prefs = FakeSharedPreferences()
        assertFalse(
            FeatureToggleRegistry.isEnabled(
                prefs,
                FeatureToggleRegistry.INCOMING_CALL_ASSISTANT
            )
        )
    }

    @Test
    fun cameraAndVoiceControlsRemainExplicitlyConfigurable() {
        val prefs = FakeSharedPreferences()

        FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND, false)
        FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.CAMERA, false)

        assertFalse(FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND))
        assertFalse(FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.CAMERA))
    }
}
