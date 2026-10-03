package com.mayra.assistant

/**
 * High-alert monitoring policy for James Billings.
 * Uses scheduled/network-permitted checks and Owner-visible notifications;
 * never uses covert screen capture or stealth services.
 */
object JamesBillingsWatchPolicy {
    data class Check(
        val source: String,
        val intervalHours: Int,
        val networkRequired: Boolean,
        val ownerVisible: Boolean,
        val highAlert: Boolean
    )

    fun defaultCheck(): Check =
        Check(
            source = "James Billings / General Research public task and update sources",
            intervalHours = 12,
            networkRequired = true,
            ownerVisible = true,
            highAlert = true
        )

    fun shouldNotify(previousFingerprint: String?, currentFingerprint: String?): Boolean =
        !currentFingerprint.isNullOrBlank() && currentFingerprint != previousFingerprint

    fun mayUseStealthService(): Boolean = false
    fun mayReadPrivateAccountData(): Boolean = false
    fun mayAutoApply(): Boolean = false
    fun mayExecuteFinancialAction(): Boolean = false
}
