package com.mayra.assistant

import android.content.Context

/**
 * Small local state boundary for public-source change fingerprints.
 * No private account credentials or payment data are stored here.
 */
object BackgroundWatchState {
    private const val PREFS = "mayra_watch_state"

    fun previousFingerprint(context: Context, key: String): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null)

    fun saveFingerprint(context: Context, key: String, fingerprint: String) {
        if (fingerprint.isBlank()) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(key, fingerprint)
            .apply()
    }
}
