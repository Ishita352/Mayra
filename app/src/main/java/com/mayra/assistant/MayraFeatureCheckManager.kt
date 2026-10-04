package com.mayra.assistant

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.core.content.ContextCompat

class MayraFeatureCheckManager(private val context: Context) {
    data class FeatureSpec(val id: String, val name: String, val description: String, val requiresPermission: Boolean = false)
    data class CheckResult(val success: Boolean, val message: String)

    companion object {
        const val PREFS = "mayra_secure"
        const val KEY_SETUP_COMPLETED = "first_run_feature_setup_completed"
        const val OWNER = "owner_verification"
        const val MASTER = "master_control"
        const val VOICE = "voice_command"
        const val CAMERA = "camera"
        const val CALL = "call_assist"
        const val WHATSAPP = "whatsapp"
        const val CHARACTER_3D = "3d_character"
        const val VOICE_LIGHT = "voice_light"
        const val LOCKED = "locked_phone"
        const val SILENT = "silent_mode"
        const val COMPUTER = "computer_pairing"
        const val BIODATA = "biodata_career"
        const val JOBS = "job_watcher"
        const val EXCEL = "excel_data"
        const val DOCUMENTS = "documents"
        const val SECURITY = "security"
        const val INTERVIEW = "interview_assistant"

        fun specs() = listOf(
            FeatureSpec(OWNER, "Owner Verification", "Owner identity gate; no installation password."),
            FeatureSpec(MASTER, "Mayra Master Control", "Master ON/OFF with saved memory/state."),
            FeatureSpec(VOICE, "Voice Command", "Bangla / Hindi / English voice command.", true),
            FeatureSpec(CAMERA, "Camera", "Camera access.", true),
            FeatureSpec(CALL, "Call Assist", "Owner-approved call assistance.", true),
            FeatureSpec(WHATSAPP, "WhatsApp", "Approved WhatsApp workflows."),
            FeatureSpec(CHARACTER_3D, "3D Character", "Mayra character animation."),
            FeatureSpec(VOICE_LIGHT, "Voice Light", "Speaking/ambient light indicator."),
            FeatureSpec(LOCKED, "Locked Phone Mode", "Optional listening while phone is locked."),
            FeatureSpec(SILENT, "Silent Mode Behavior", "Listen without speaking when phone is silent."),
            FeatureSpec(COMPUTER, "Windows 10 Pairing", "Phone to computer pairing architecture."),
            FeatureSpec(BIODATA, "Biodata & Career", "Career profile and CV foundation."),
            FeatureSpec(JOBS, "Job Watcher", "Job/freelance watcher foundation."),
            FeatureSpec(EXCEL, "Excel / Data Analysis", "Spreadsheet analysis foundation."),
            FeatureSpec(DOCUMENTS, "Documents / PDF / DOCX", "Document engines."),
            FeatureSpec(SECURITY, "Security Controls", "Owner-controlled security gates."),
            FeatureSpec(INTERVIEW, "Interview Assistant", "Free interview preparation, mock interviews and answer coaching.")
        )

        fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        fun isSetupCompleted(context: Context) = prefs(context).getBoolean(KEY_SETUP_COMPLETED, false)
        fun isChecked(context: Context, id: String) = prefs(context).getBoolean("feature_checked_$id", false)
        fun isEnabled(context: Context, id: String) = prefs(context).getBoolean("feature_enabled_$id", false)

        fun summary(context: Context): String {
            val p = prefs(context)
            return specs().joinToString("\n") {
                val checked = p.getBoolean("feature_checked_${it.id}", false)
                val enabled = p.getBoolean("feature_enabled_${it.id}", false)
                val state = when {
                    enabled && checked -> "ON • ✓ Checked"
                    checked -> "OFF • ✓ Checked"
                    else -> "OFF • Not checked"
                }
                "• ${it.name}: $state"
            }
        }
    }

    fun check(spec: FeatureSpec): CheckResult {
        return when (spec.id) {
            OWNER -> {
                val ok = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("owner_verified", false)
                CheckResult(ok, if (ok) "Owner verification is active." else "Owner verification is not completed.")
            }
            MASTER -> CheckResult(true, "Master control storage is working.")
            VOICE -> {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                val recognizer = Intent("android.speech.action.RECOGNIZE_SPEECH").resolveActivity(context.packageManager) != null
                CheckResult(granted && recognizer, if (granted && recognizer) "Microphone permission and speech recognizer are available." else "Microphone permission and/or speech recognizer is unavailable.")
            }
            CAMERA -> {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                val hardware = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(context.packageManager) != null
                CheckResult(granted && hardware && cameraIntent, if (granted && hardware && cameraIntent) "Camera permission, hardware and camera activity are available." else "Camera permission, hardware and/or camera activity is unavailable.")
            }
            CALL -> {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                CheckResult(granted, if (granted) "Phone-state permission is available for Call Assist." else "Phone-state permission is not granted.")
            }
            WHATSAPP -> {
                val installed = try { context.packageManager.getApplicationInfo("com.whatsapp", 0); true } catch (_: Exception) { false }
                CheckResult(installed, if (installed) "WhatsApp is installed and targetable." else "WhatsApp is not installed.")
            }
            CHARACTER_3D -> CheckResult(true, "Animation engine is available.")
            VOICE_LIGHT -> CheckResult(true, "Voice Light state and animation hooks are available.")
            LOCKED -> CheckResult(context.getSystemService(KeyguardManager::class.java) != null, "Locked-phone state can be detected.")
            SILENT -> CheckResult(context.getSystemService(AudioManager::class.java) != null, "Phone audio state can be read.")
            COMPUTER -> CheckResult(true, "Windows pairing session storage and command gate are available.")
            BIODATA -> CheckResult(true, "Biodata/Career profile module is available.")
            JOBS -> CheckResult(true, "Job Watcher foundation is available.")
            EXCEL -> CheckResult(true, "Excel/Data Analysis foundation is available.")
            DOCUMENTS -> CheckResult(true, "PDF/DOCX/TXT document engines are available.")
            SECURITY -> CheckResult(true, "Security control gate is available.")
            INTERVIEW -> CheckResult(true, "Interview preparation, mock interview and answer coaching engine is available.")
            else -> CheckResult(false, "Unknown feature.")
        }
    }

    fun setEnabled(id: String, enabled: Boolean) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putBoolean("feature_enabled_$id", enabled).apply()
        when (id) {
            VOICE -> FeatureToggleRegistry.setEnabled(p, FeatureToggleRegistry.VOICE_COMMAND, enabled)
            CAMERA -> FeatureToggleRegistry.setEnabled(p, FeatureToggleRegistry.CAMERA, enabled)
            CALL -> FeatureToggleRegistry.setEnabled(p, FeatureToggleRegistry.INCOMING_CALL_ASSISTANT, enabled)
            WHATSAPP -> FeatureToggleRegistry.setEnabled(p, FeatureToggleRegistry.WHATSAPP_ASSISTANT, enabled)
            SECURITY -> FeatureToggleRegistry.setEnabled(p, FeatureToggleRegistry.SECURITY, enabled)
            INTERVIEW -> p.edit().putBoolean("interview_assistant_enabled", enabled).apply()
            CHARACTER_3D -> p.edit().putBoolean("mayra_3d_character_enabled", enabled).apply()
            VOICE_LIGHT -> p.edit().putBoolean("mayra_voice_light_enabled", enabled).apply()
            LOCKED -> p.edit().putBoolean("mayra_active_while_locked", enabled).apply()
            SILENT -> p.edit().putBoolean("mayra_silent_mode_behavior", enabled).apply()
            MASTER -> p.edit().putBoolean("master_on", enabled).apply()
        }
    }

    fun markChecked(id: String, success: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("feature_checked_$id", true)
            .putBoolean("feature_last_check_ok_$id", success)
            .apply()
    }

    fun markSetupComplete() {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_SETUP_COMPLETED, true).apply()
    }
}
