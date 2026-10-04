package com.mayra.assistant

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Mayra Self-Healing / Health Monitor.
 *
 * Repairs only safe configuration/state inconsistencies. It never grants
 * permissions, installs code, changes security policy, or performs arbitrary
 * system actions.
 */
object MayraHealthMonitor {
    data class CheckResult(
        val healthy: Boolean,
        val repaired: List<String>,
        val warnings: List<String>
    )

    private const val LAST_HEALTH = "mayra_health_last_status"
    private const val LAST_REPAIR_COUNT = "mayra_health_last_repair_count"

    fun run(context: Context): CheckResult {
        val prefs = context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        return run(prefs, context)
    }

    fun run(prefs: SharedPreferences, context: Context? = null): CheckResult {
        val repaired = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val editor = prefs.edit()

        val setupComplete = prefs.getBoolean(
            MayraFeatureCheckManager.KEY_SETUP_COMPLETED,
            prefs.getBoolean("setup_complete", false)
        )
        if (!setupComplete) {
            if (prefs.getBoolean("master_on", false)) {
                editor.putBoolean("master_on", false)
                repaired += "Master state reset because first-time setup is incomplete"
            }
            if (FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND)) {
                editor.putBoolean(FeatureToggleRegistry.VOICE_COMMAND, false)
                repaired += "Voice Command disabled until setup is complete"
            }
        }

        // Use the effective post-repair Master state, not the stale stored value.
        val masterOn = prefs.getBoolean("master_on", false) && setupComplete
        val ownerVerified = prefs.getBoolean("owner_verified", false)

        if (!ownerVerified && FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND)) {
            editor.putBoolean(FeatureToggleRegistry.VOICE_COMMAND, false)
            repaired += "Voice Command disabled because Owner verification is missing"
        }

        val micPermission = context == null ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND) && !micPermission) {
            editor.putBoolean(FeatureToggleRegistry.VOICE_COMMAND, false)
            repaired += "Voice Command disabled because microphone permission is unavailable"
            warnings += "Microphone permission must be granted by the owner to re-enable Voice Command"
        }

        if (!masterOn && prefs.getBoolean("mayra_voice_light_enabled", false)) {
            editor.putBoolean("mayra_voice_light_enabled", false)
            repaired += "Voice Light disabled while Master is OFF"
        }

        val windowsDevice = prefs.getString("windows_paired_device", null)
        val windowsHost = prefs.getString("windows_paired_host", null)
        val windowsToken = prefs.getString("windows_paired_token", null)
        if (windowsDevice != null && (windowsHost.isNullOrBlank() || windowsToken.isNullOrBlank())) {
            editor.remove("windows_paired_device")
                .remove("windows_paired_host")
                .remove("windows_paired_port")
                .remove("windows_paired_token")
                .remove("windows_phone_host")
                .remove("windows_phone_port")
            repaired += "Incomplete Windows pairing state cleared"
        }

        if (masterOn && !ownerVerified) {
            warnings += "Master is ON but Owner verification is not active"
        }

        if (repaired.isNotEmpty()) editor.apply()

        val healthy = repaired.isEmpty() && warnings.isEmpty()
        prefs.edit()
            .putString(LAST_HEALTH, if (healthy) "HEALTHY" else if (repaired.isNotEmpty()) "REPAIRED" else "WARNING")
            .putInt(LAST_REPAIR_COUNT, repaired.size)
            .apply()

        return CheckResult(healthy, repaired, warnings)
    }

    fun status(prefs: SharedPreferences): String {
        val state = prefs.getString(LAST_HEALTH, "NOT_CHECKED")
        val count = prefs.getInt(LAST_REPAIR_COUNT, 0)
        return "Self-Healing: $state • repairs=$count"
    }

    fun safeRecoveryMessage(result: CheckResult): String =
        when {
            result.repaired.isNotEmpty() ->
                "Mayra health check complete. ${result.repaired.size} safe issue(s) repaired automatically."
            result.warnings.isNotEmpty() ->
                "Mayra health check complete. ${result.warnings.size} issue(s) need owner/system permission."
            else -> "Mayra health check complete. All monitored systems are healthy."
        }
}
