package com.mayra.assistant

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Mayra Voice Engine V1.
 *
 * Keeps the 20 audition slots stable while allowing each Android TTS engine
 * to supply the best matching installed voice. The engine never claims a
 * profile is available unless the current device exposes a compatible voice.
 */
data class MayraVoiceProfile(
    val id: String,
    val displayName: String,
    val gender: String,
    val style: String,
    val isGopalMatch: Boolean = false
)

object MayraVoiceEngine {
    const val PREF_SELECTED_VOICE = "mayra_selected_voice_profile"
    const val PREF_SPEED = "mayra_voice_speed"
    const val PREF_PITCH = "mayra_voice_pitch"

    val profiles: List<MayraVoiceProfile> = listOf(
        MayraVoiceProfile("female_01", "Female 01 • Sweet", "female", "sweet"),
        MayraVoiceProfile("female_02", "Female 02 • Soft", "female", "soft"),
        MayraVoiceProfile("female_03", "Female 03 • Natural", "female", "natural"),
        MayraVoiceProfile("female_04", "Female 04 • Elegant", "female", "elegant"),
        MayraVoiceProfile("female_05", "Female 05 • Professional", "female", "professional"),
        MayraVoiceProfile("female_06", "Female 06 • Warm", "female", "warm"),
        MayraVoiceProfile("female_07", "Female 07 • Friendly", "female", "friendly"),
        MayraVoiceProfile("female_08", "Female 08 • Clear", "female", "clear"),
        MayraVoiceProfile("female_09", "Female 09 • Energetic", "female", "energetic"),
        MayraVoiceProfile("female_10", "Female 10 • Calm", "female", "calm"),
        MayraVoiceProfile("male_01", "Male 01 • Warm", "male", "warm"),
        MayraVoiceProfile("male_02", "Male 02 • Natural", "male", "natural"),
        MayraVoiceProfile("male_03", "Male 03 • Deep", "male", "deep"),
        MayraVoiceProfile("male_04", "Male 04 • Professional", "male", "professional"),
        MayraVoiceProfile("male_05", "Male 05 • Friendly", "male", "friendly"),
        MayraVoiceProfile("male_06", "Male 06 • Clear", "male", "clear"),
        MayraVoiceProfile("male_07", "Male 07 • Calm", "male", "calm"),
        MayraVoiceProfile("male_08", "Male 08 • Energetic", "male", "energetic"),
        MayraVoiceProfile("male_09", "Male 09 • Gentle", "male", "gentle"),
        MayraVoiceProfile("gopal_voice_match", "Gopal Voice Match", "match", "owner", true)
    )

    fun selectedId(context: Context): String =
        context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
            .getString(PREF_SELECTED_VOICE, "female_01") ?: "female_01"

    fun select(context: Context, profileId: String): Boolean {
        if (profiles.none { it.id == profileId }) return false
        context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
            .edit().putString(PREF_SELECTED_VOICE, profileId).apply()
        return true
    }

    fun setTuning(context: Context, speed: Float, pitch: Float) {
        context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE).edit()
            .putFloat(PREF_SPEED, speed.coerceIn(0.6f, 1.4f))
            .putFloat(PREF_PITCH, pitch.coerceIn(0.7f, 1.3f))
            .apply()
    }

    fun speak(
        context: Context,
        tts: TextToSpeech,
        text: String,
        utteranceId: String,
        onReady: (() -> Unit)? = null
    ) {
        val prefs = context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        val selected = profiles.firstOrNull { it.id == selectedId(context) } ?: profiles.first()
        val locale = when {
            text.contains(Regex("[\\u0980-\\u09FF]")) -> Locale("bn", "IN")
            text.contains(Regex("[\\u0900-\\u097F]")) -> Locale("hi", "IN")
            else -> Locale.US
        }

        val voices = tts.voices.orEmpty()
        val compatible = voices.filter { voice ->
            voice.locale.language == locale.language &&
                !voice.isNetworkConnectionRequired
        }
        val ranked = compatible.sortedWith(
            compareByDescending<android.speech.tts.Voice> { voice ->
                when {
                    selected.gender == "female" && voice.name.contains("female", true) -> 3
                    selected.gender == "male" && voice.name.contains("male", true) -> 3
                    else -> 0
                }
            }.thenByDescending { it.quality }
        )
        if (ranked.isNotEmpty()) tts.voice = ranked.first()

        val speed = prefs.getFloat(PREF_SPEED, 1.0f)
        val pitch = prefs.getFloat(PREF_PITCH, 1.0f)
        tts.setSpeechRate(speed)
        tts.setPitch(pitch)
        if (tts.setLanguage(locale) >= TextToSpeech.LANG_MISSING_DATA) return
        onReady?.invoke()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
}
