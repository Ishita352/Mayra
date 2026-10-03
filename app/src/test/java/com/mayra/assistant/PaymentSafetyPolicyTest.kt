package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentSafetyPolicyTest {
    @Test
    fun financialLockIsAlwaysEnabled() {
        assertTrue(PaymentSafetyPolicy.financialLockEnabled())
    }

    @Test
    fun automationIsNeverAllowedForKnownFinancialPackage() {
        assertFalse(
            PaymentSafetyPolicy.mayraMayAutomatePackage(
                "com.google.android.apps.nbu.paisa.user"
            )
        )
    }

    @Test
    fun automationIsNeverAllowedForUnknownPackage() {
        assertFalse(PaymentSafetyPolicy.mayraMayAutomatePackage("com.example.unknown"))
    }

    @Test
    fun knownFinancialPackageDetectionIsDenyByDefaultForBlankInput() {
        assertFalse(PaymentSafetyPolicy.isKnownFinancialPackage(null))
        assertFalse(PaymentSafetyPolicy.isKnownFinancialPackage(""))
        assertFalse(PaymentSafetyPolicy.isKnownFinancialPackage("   "))
    }

    @Test
    fun knownFinancialPackageDetectionRecognizesPaymentApps() {
        assertTrue(
            PaymentSafetyPolicy.isKnownFinancialPackage("com.phonepe.app")
        )
        assertTrue(
            PaymentSafetyPolicy.isKnownFinancialPackage("net.one97.paytm")
        )
    }
}
