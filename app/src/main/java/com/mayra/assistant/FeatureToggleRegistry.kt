package com.mayra.assistant

import android.content.SharedPreferences

object FeatureToggleRegistry {
    const val VOICE_COMMAND = "voice_command_enabled"
    const val CAMERA = "camera_enabled"
    const val INCOMING_CALL_ASSISTANT = "incoming_call_assistant_enabled"

    fun isEnabled(prefs: SharedPreferences, key: String): Boolean =
        prefs.getBoolean(key, defaultFor(key))

    fun setEnabled(prefs: SharedPreferences, key: String, enabled: Boolean) {
        prefs.edit().putBoolean(key, enabled).apply()
    }

    fun defaultFor(key: String): Boolean = when (key) {
        VOICE_COMMAND -> true
        CAMERA -> true
        INCOMING_CALL_ASSISTANT -> false
        else -> false
    }
}
