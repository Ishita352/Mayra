package com.mayra.assistant

import android.content.SharedPreferences

/** Owner-controlled personality/mood layer. Mood changes presentation only, never permissions or safety. */
object MayraMoodSystem {
    enum class Mood(val label: String, val speechRate: Float, val pitch: Float) {
        FUN("Fun", 1.08f, 1.08f),
        HAPPY("Happy", 1.04f, 1.05f),
        FRIENDLY("Friendly", 1.00f, 1.02f),
        FOCUSED("Focused", 0.94f, 0.98f),
        PROFESSIONAL("Professional", 0.92f, 0.96f),
        CARING("Caring", 0.96f, 1.00f),
        CALM("Calm", 0.88f, 0.94f),
        TEACHER("Teacher", 0.93f, 1.00f),
        MOTIVATIONAL("Motivational", 1.02f, 1.06f),
        QUIET("Quiet", 0.86f, 0.92f),
        CELEBRATION("Celebration", 1.10f, 1.10f)
    }

    private const val KEY = "mayra_mood"

    fun current(prefs: SharedPreferences): Mood =
        runCatching { Mood.valueOf(prefs.getString(KEY, Mood.FRIENDLY.name) ?: Mood.FRIENDLY.name) }
            .getOrDefault(Mood.FRIENDLY)

    fun set(prefs: SharedPreferences, mood: Mood) {
        prefs.edit().putString(KEY, mood.name).apply()
    }

    fun commandMood(spoken: String): Mood? {
        val s = spoken.lowercase()
        return when {
            s.contains("fun mood") || s.contains("ফান মোড") || s.contains("মজার মুড") -> Mood.FUN
            s.contains("happy mood") || s.contains("হ্যাপি মোড") -> Mood.HAPPY
            s.contains("friendly mood") || s.contains("ফ্রেন্ডলি মোড") -> Mood.FRIENDLY
            s.contains("focused mood") || s.contains("ফোকাসড মোড") -> Mood.FOCUSED
            s.contains("professional mood") || s.contains("প্রফেশনাল মোড") -> Mood.PROFESSIONAL
            s.contains("caring mood") || s.contains("কেয়ারিং মোড") || s.contains("কেয়ারিং মোড") -> Mood.CARING
            s.contains("calm mood") || s.contains("কাম মোড") || s.contains("শান্ত মোড") -> Mood.CALM
            s.contains("teacher mood") || s.contains("টিচার মোড") -> Mood.TEACHER
            s.contains("motivational mood") || s.contains("মোটিভেশনাল মোড") -> Mood.MOTIVATIONAL
            s.contains("quiet mood") || s.contains("কোয়ায়েট মোড") || s.contains("শান্তভাবে মোড") -> Mood.QUIET
            s.contains("celebration mood") || s.contains("সেলিব্রেশন মোড") -> Mood.CELEBRATION
            else -> null
        }
    }

    fun all(): List<Mood> = Mood.values().toList()

    fun rule() =
        "Mood changes Mayra's communication style and TTS presentation only. Mood never changes permissions, Owner authorization, safety rules, security controls or financial restrictions."
}
