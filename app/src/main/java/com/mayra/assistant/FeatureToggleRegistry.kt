package com.mayra.assistant

import android.content.SharedPreferences

object FeatureToggleRegistry {\n    // Self-healing health rules are kept in MayraHealthMonitor and invoked by lifecycle components.
    const val VOICE_COMMAND = "voice_command_enabled"
    const val CAMERA = "camera_enabled"
    const val INCOMING_CALL_ASSISTANT = "incoming_call_assistant_enabled"
    const val WHATSAPP_ASSISTANT = "whatsapp_assistant_enabled"
    const val SECURITY = "security_control_enabled"

    fun isEnabled(prefs: SharedPreferences, key: String): Boolean =
        prefs.getBoolean(key, defaultFor(key))

    fun setEnabled(prefs: SharedPreferences, key: String, enabled: Boolean) {
        prefs.edit().putBoolean(key, enabled).apply()
    }

    fun defaultFor(key: String): Boolean = false

    fun resetAllToOff(prefs: SharedPreferences) {
        prefs.edit()
            .putBoolean(VOICE_COMMAND, false)
            .putBoolean(CAMERA, false)
            .putBoolean(INCOMING_CALL_ASSISTANT, false)
            .putBoolean(WHATSAPP_ASSISTANT, false)
            .putBoolean(SECURITY, false)
            .putBoolean("mayra_3d_character_enabled", false)
            .putBoolean("mayra_voice_light_enabled", false)
            .putBoolean("mayra_active_while_locked", false)
            .putBoolean("mayra_silent_mode_behavior", false)
            .putBoolean("master_on", false)
            .apply()
    }
}
