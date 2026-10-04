package com.mayra.assistant

import android.content.SharedPreferences

/**
 * Persistent UI/session checkpoint for Mayra.
 *
 * Session state remains compatible with the existing UI store while a
 * separate semantic-memory bridge can persist resumable task context.
 */
class MayraSessionState(private val prefs: SharedPreferences) {
    companion object {
        private const val KEY_ROUTE = "session_route"
        private const val KEY_TITLE = "session_title"
        private const val KEY_DETAILS = "session_details"
        private const val KEY_SAVED_AT = "session_saved_at"
    }

    fun saveHome() {
        prefs.edit()
            .putString(KEY_ROUTE, "home")
            .putString(KEY_TITLE, "Mayra Home")
            .putString(KEY_DETAILS, "")
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun saveModule(title: String, details: String) {
        require(title.isNotBlank())
        prefs.edit()
            .putString(KEY_ROUTE, "module")
            .putString(KEY_TITLE, title)
            .putString(KEY_DETAILS, details.take(20_000))
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun hasResumeState(): Boolean =
        prefs.getString(KEY_ROUTE, null) == "module" &&
            !prefs.getString(KEY_TITLE, null).isNullOrBlank()

    fun title(): String = prefs.getString(KEY_TITLE, "Mayra") ?: "Mayra"

    fun details(): String = prefs.getString(KEY_DETAILS, "") ?: ""

    fun savedAtMs(): Long = prefs.getLong(KEY_SAVED_AT, 0L)
}
