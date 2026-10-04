package com.mayra.assistant

import android.content.Context
import java.io.File
import java.security.MessageDigest

data class GopalVoiceMatchStatus(
    val sampleReady: Boolean,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val qualityScore: Int,
    val fingerprint: String?,
    val message: String
)

object MayraGopalVoiceMatch {
    private const val PREF_READY = "gopal_voice_match_ready"
    private const val PREF_QUALITY = "gopal_voice_match_quality"
    private const val PREF_DURATION = "gopal_voice_match_duration"
    private const val PREF_FINGERPRINT = "gopal_voice_match_fingerprint"

    const val MIN_SAMPLE_MS = 15_000L
    const val MAX_SAMPLE_MS = 30_000L

    fun sampleFile(context: Context): File =
        File(context.filesDir, "gopal_voice_match.m4a")

    /**
     * Validates and records the owner's sample metadata. This is a local
     * enrollment step; it does not claim to synthesize a cloned voice.
     */
    fun saveAnalysis(context: Context, durationMs: Long, sizeBytes: Long): GopalVoiceMatchStatus {
        val file = sampleFile(context)
        val duration = durationMs.coerceAtLeast(0L)
        val inRange = duration in MIN_SAMPLE_MS..MAX_SAMPLE_MS
        val sizeOk = sizeBytes >= 20_000L
        val durationScore = when {
            duration < 8_000L -> 25
            duration < MIN_SAMPLE_MS -> 55
            duration <= MAX_SAMPLE_MS -> 90
            else -> 70
        }
        val sizeScore = if (sizeOk) 10 else 0
        val quality = (durationScore + sizeScore).coerceIn(0, 100)
        val ready = file.exists() && inRange && sizeOk && quality >= 80
        val fingerprint = if (ready) sha256(file) else null

        context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE).edit()
            .putBoolean(PREF_READY, ready)
            .putInt(PREF_QUALITY, quality)
            .putLong(PREF_DURATION, duration)
            .apply {
                if (fingerprint != null) putString(PREF_FINGERPRINT, fingerprint)
                else remove(PREF_FINGERPRINT)
            }
            .apply()

        return status(context)
    }

    fun status(context: Context): GopalVoiceMatchStatus {
        val prefs = context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        val file = sampleFile(context)
        val duration = prefs.getLong(PREF_DURATION, 0L)
        val size = if (file.exists()) file.length() else 0L
        val ready = prefs.getBoolean(PREF_READY, false) && file.exists() &&
            duration in MIN_SAMPLE_MS..MAX_SAMPLE_MS && size >= 20_000L
        val quality = prefs.getInt(PREF_QUALITY, 0)
        val fingerprint = prefs.getString(PREF_FINGERPRINT, null)
        val message = when {
            ready -> "Owner sample is enrolled locally. A true voice-cloning backend is not installed."
            file.exists() && duration < MIN_SAMPLE_MS -> "Sample is too short. Record at least 15 seconds."
            file.exists() && duration > MAX_SAMPLE_MS -> "Sample is too long. Record no more than 30 seconds."
            else -> "Record a clear 15–30 second Bengali, Hindi and English sample."
        }
        return GopalVoiceMatchStatus(ready, duration, size, quality, fingerprint, message)
    }

    fun isReady(context: Context): Boolean = status(context).sampleReady

    fun deleteSample(context: Context) {
        sampleFile(context).delete()
        context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE).edit()
            .remove(PREF_READY)
            .remove(PREF_QUALITY)
            .remove(PREF_DURATION)
            .remove(PREF_FINGERPRINT)
            .apply()
    }

    private fun sha256(file: File): String = runCatching {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count <= 0) break
                digest.update(buffer, 0, count)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull() ?: ""
}
