package com.mayra.assistant

/**
 * Hard safety boundary: Mayra must never automate or initiate financial transactions.
 * This policy is intentionally deny-by-default for payment/financial app automation.
 */
object PaymentSafetyPolicy {
    private val blockedPackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay (common package)
        "com.phonepe.app",                         // PhonePe
        "net.one97.paytm",                         // Paytm
        "com.paytmmall",                           // Paytm variants
        "in.amazon.mShop.android.shopping",        // Amazon app (may contain payments)
        "com.mobikwik_new",                        // MobiKwik
        "com.freecharge.android",                  // Freecharge
        "com.sbi.SBIFreedomPlus",                 // SBI banking variants
        "com.icicibank.mobile.alpha",             // ICICI banking variant
        "com.hdfcbank.android.hdfcbankmobile",    // HDFC banking variant
        "com.axis.mobile",                         // Axis banking variant
        "com.yesbank.mobile",                      // YES banking variant
        "com.bankofbaroda.mconnect"                // Bank of Baroda variant
    )

    /** Returns true when Mayra is forbidden from automating this package. */
    fun isBlockedPackage(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        return packageName in blockedPackages
    }

    /** All financial/payment automation must pass this gate first. */
    fun mayraMayAutomatePackage(packageName: String?): Boolean =
        !isBlockedPackage(packageName)
}
