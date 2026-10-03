package com.mayra.assistant

import android.content.SharedPreferences

/**
 * Owner-controlled locked-phone execution preference.
 *
 * OFF is the safe default. When ON, Mayra is allowed to enter the future
 * locked-device execution path for explicitly permitted, non-financial tasks.
 * Financial actions remain blocked by PaymentSafetyPolicy regardless of this setting.
 */
object LockModePolicy {
    private const val KEY_LOCKED_PHONE_MODE = "locked_phone_mode"

    fun isEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_LOCKED_PHONE_MODE, false)

    fun setEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCKED_PHONE_MODE, enabled).apply()
    }
}
