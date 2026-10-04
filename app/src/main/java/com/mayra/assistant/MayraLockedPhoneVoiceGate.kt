package com.mayra.assistant

import android.content.SharedPreferences

/** Locked-phone gate: only explicitly enabled, non-sensitive voice tasks may proceed. */
object MayraLockedPhoneVoiceGate {
    enum class Decision { ALLOW_LIMITED_VOICE, OWNER_VERIFICATION_REQUIRED, BLOCK }

    fun decide(
        prefs: SharedPreferences,
        ownerVerified: Boolean,
        ownerCommandAuthorized: Boolean = ownerVerified,
        masterOn: Boolean,
        locked: Boolean,
        task: String
    ): Decision {
        if (!masterOn || task.isBlank()) return Decision.BLOCK
        if (!locked) return Decision.ALLOW_LIMITED_VOICE
        if (!LockModePolicy.isEnabled(prefs)) return Decision.BLOCK
        if (!ownerVerified || !ownerCommandAuthorized) return Decision.OWNER_VERIFICATION_REQUIRED

        val t = task.lowercase()
        val sensitiveTerms = listOf(
            "financial", "payment", "bank", "wallet", "security change", "password change",
            "install app", "delete",
            "পেমেন্ট", "ব্যাংক", "ওয়ালেট", "পাসওয়ার্ড", "নিরাপত্তা পরিবর্তন", "অ্যাপ ইনস্টল", "ডিলিট",
            "भुगतान", "बैंक", "वॉलेट", "पासवर्ड", "सुरक्षा बदलो", "ऐप इंस्टॉल", "डिलीट"
        )
        if (sensitiveTerms.any { t.contains(it) }) return Decision.BLOCK
        return Decision.ALLOW_LIMITED_VOICE
    }

    fun rule() =
        "Locked-phone voice access is optional, Owner-controlled, verification-gated, and limited to non-sensitive tasks."
}
