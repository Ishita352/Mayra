package com.mayra.assistant

/**
 * Hard safety boundary: Mayra is financially inert.
 *
 * Mayra must never spend, purchase, subscribe, withdraw, transfer, or execute
 * any financial transaction. Financial/payment automation is deny-by-default.
 *
 * This policy is intentionally conservative: an unknown package is NOT treated
 * as safe merely because it is not on a known payment-app blocklist.
 */
object PaymentSafetyPolicy {
    private val knownFinancialPackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "com.phonepe.app",                         // PhonePe
        "net.one97.paytm",                         // Paytm
        "com.paytmmall",                           // Paytm variants
        "in.amazon.mShop.android.shopping",        // Amazon app
        "com.mobikwik_new",                        // MobiKwik
        "com.freecharge.android",                  // Freecharge
        "com.sbi.SBIFreedomPlus",                  // SBI
        "com.icicibank.mobile.alpha",              // ICICI
        "com.hdfcbank.android.hdfcbankmobile",     // HDFC
        "com.axis.mobile",                         // Axis
        "com.yesbank.mobile",                      // YES Bank
        "com.bankofbaroda.mconnect"                // Bank of Baroda
    )

    /**
     * Returns true when the package is known to be financial/payment related.
     */
    fun isKnownFinancialPackage(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        return packageName in knownFinancialPackages
    }

    /**
     * Financial automation is NEVER allowed.
     *
     * Kept as a single gate so every future automation path can call the same
     * deny-by-default policy instead of implementing its own package checks.
     */
    fun mayraMayAutomatePackage(packageName: String?): Boolean = false

    /**
     * Explicitly documents the permanent Financial Lock for callers/tests.
     */
    fun financialLockEnabled(): Boolean = true
}
