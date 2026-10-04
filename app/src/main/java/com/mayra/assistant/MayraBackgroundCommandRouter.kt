package com.mayra.assistant

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

/**
 * Small command router that can run without an Activity.
 * It covers only safe presentation/background controls; UI-only actions remain in MainActivity.
 */
object MayraBackgroundCommandRouter {
    data class Result(val handled: Boolean, val response: String)

    fun route(context: Context, spoken: String): Result {
        val prefs = context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        return route(prefs, spoken, context)
    }

    fun route(prefs: SharedPreferences, spoken: String): Result =
        route(prefs, spoken, null)

    private fun route(prefs: SharedPreferences, spoken: String, context: Context?): Result {
        val lower = spoken.lowercase(Locale.ROOT).trim()
        val off = lower.contains("off") || lower.contains("বন্ধ") ||
            lower.contains("disable") || lower.contains("बंद")

        if (listOf("health check", "self heal", "self-healing", "system check", "সিস্টেম চেক", "নিজে ঠিক", "स्वास्थ्य जांच").any { lower.contains(it) }) {
            val result = MayraHealthMonitor.run(prefs, context)
            return Result(true, MayraHealthMonitor.safeRecoveryMessage(result))
        }

        if (listOf("master off", "mayra off", "মায়রা অফ", "মায়রা অফ", "मायरा बंद")
                .any { lower.contains(it) }) {
            prefs.edit().putBoolean("master_on", false).apply()
            return Result(true, "বস, Mayra Master OFF করেছি।")
        }
        if (listOf(
                "voice command off", "stop listening",
                "ভয়েস কমান্ড বন্ধ", "ভয়েস কমান্ড বন্ধ", "voice off"
            ).any { lower.contains(it) }) {
            FeatureToggleRegistry.setEnabled(
                prefs,
                FeatureToggleRegistry.VOICE_COMMAND,
                false
            )
            return Result(true, "বস, Voice Command বন্ধ করেছি।")
        }
        if (listOf(
                "feature status", "what features", "কি কি ফিচার",
                "কোন কোন ফিচার", "ফিচারগুলোর অবস্থা", "फीचर स्टेटस"
            ).any { lower.contains(it) }) {
            return Result(true, context?.let { MayraFeatureCheckManager.summary(it) }
                ?: "Feature status requires the Mayra app context.")
        }

        MayraMoodSystem.commandMood(spoken)?.let { mood ->
            MayraMoodSystem.set(prefs, mood)
            return Result(true, "Mayra " + mood.label + " Mood চালু হয়েছে।")
        }

        if (lower.contains("3d") || lower.contains("three d") || lower.contains("থ্রিডি")) {
            prefs.edit().putBoolean("mayra_3d_character_enabled", !off).apply()
            return Result(true, if (off) "3D Character OFF।" else "3D Character ON।")
        }
        if (lower.contains("voice light") ||
            lower.contains("ভয়েস লাইট") || lower.contains("ভয়েস লাইট")) {
            prefs.edit().putBoolean("mayra_voice_light_enabled", !off).apply()
            return Result(true, if (off) "Voice Light OFF।" else "Voice Light ON।")
        }

        val character = MayraCharacterSystem.all().firstOrNull { profile ->
            lower.contains(profile.name.lowercase(Locale.ROOT)) ||
                lower.contains(profile.id.lowercase(Locale.ROOT))
        }
        if (character != null) {
            MayraCharacterSystem.select(prefs, character.id)
            return Result(true, "Character " + character.name + " selected.")
        }

        return Result(false, "")
    }
}
