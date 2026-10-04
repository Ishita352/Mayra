package com.mayra.assistant

import android.content.SharedPreferences

/** Owner-controlled presentation layer. Mood changes voice/animation style only. */
object MayraMoodSystem {
    enum class Mood(
        val label: String,
        val speechRate: Float,
        val pitch: Float,
        val bodyReaction: String
    ) {
        HAPPY("Happy", 1.04f, 1.05f, "Smile, open hands slightly, gentle head bob"),
        CALM("Calm", 0.90f, 0.95f, "Slow nod, relaxed shoulders, soft eye movement"),
        SWEET("Sweet", 0.96f, 1.03f, "Soft smile, slight head tilt, hand near chest"),
        FUN("Fun", 1.08f, 1.08f, "Lively sway, playful hand gesture, bright expression"),
        FRIENDLY("Friendly", 1.00f, 1.02f, "Welcome gesture, small forward lean, natural nod"),
        THINKING("Thinking", 0.94f, 0.98f, "Eyes shift upward, thinking gesture, measured nod"),
        SURPRISED("Surprised", 1.06f, 1.10f, "Wide eyes, raised brows, brief lean back, hands up"),
        COOL("Cool", 0.96f, 0.94f, "Confident posture, controlled gesture, slow head turn"),
        NOTTY_GIRL("Notty Girl", 1.06f, 1.06f, "Playful smile, eyebrow raise, teasing side gesture"),
        PROFESSIONAL("Professional", 0.92f, 0.96f, "Straight posture, precise hand gesture, confident nod")
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
            s.contains("happy mood") || s.contains("হ্যাপি মোড") -> Mood.HAPPY
            s.contains("calm mood") || s.contains("কাম মোড") || s.contains("শান্ত মোড") -> Mood.CALM
            s.contains("sweet mood") || s.contains("সুইট মোড") -> Mood.SWEET
            s.contains("fun mood") || s.contains("ফান মোড") || s.contains("মজার মুড") -> Mood.FUN
            s.contains("friendly mood") || s.contains("ফ্রেন্ডলি মোড") -> Mood.FRIENDLY
            s.contains("thinking mood") || s.contains("থিংকিং মোড") -> Mood.THINKING
            s.contains("surprised mood") || s.contains("সারপ্রাইজড মোড") -> Mood.SURPRISED
            s.contains("cool mood") || s.contains("কুল মোড") -> Mood.COOL
            s.contains("notty girl") || s.contains("নটি গার্ল") || s.contains("নটি মোড") -> Mood.NOTTY_GIRL
            s.contains("professional mood") || s.contains("প্রফেশনাল মোড") -> Mood.PROFESSIONAL
            else -> null
        }
    }

    fun all(): List<Mood> = Mood.values().toList()

    fun rule() =
        "Mood changes Mayra's communication style and presentation only. Mood never changes permissions, Owner authorization, safety rules, security controls or financial restrictions."
}
