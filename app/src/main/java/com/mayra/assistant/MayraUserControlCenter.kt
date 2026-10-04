package com.mayra.assistant

/**
 * Owner-controlled presentation and communication controls exposed as Home buttons
 * and voice commands. Android/WhatsApp platform permissions still apply.
 */
object MayraUserControlCenter {
    const val CAMERA = "camera"
    const val INCOMING_CALLS = "incoming_calls"
    const val THREE_D_CHARACTER = "three_d_character"
    const val VOICE_LIGHT = "voice_light"
    const val WHATSAPP_IMPORTANT = "whatsapp_important"

    data class State(
        val camera: Boolean,
        val incomingCalls: Boolean,
        val threeDCharacter: Boolean,
        val voiceLight: Boolean,
        val whatsappImportant: Boolean
    )

    fun state(prefs: android.content.SharedPreferences): State = State(
        camera = FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.CAMERA),
        incomingCalls = FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.INCOMING_CALL_ASSISTANT),
        threeDCharacter = prefs.getBoolean("mayra_3d_character_enabled", false),
        voiceLight = prefs.getBoolean("mayra_voice_light_enabled", true),
        whatsappImportant = prefs.getBoolean("mayra_whatsapp_important_enabled", true)
    )

    fun set(prefs: android.content.SharedPreferences, control: String, enabled: Boolean) {
        when (control) {
            CAMERA -> FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.CAMERA, enabled)
            INCOMING_CALLS -> FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.INCOMING_CALL_ASSISTANT, enabled)
            THREE_D_CHARACTER -> prefs.edit().putBoolean("mayra_3d_character_enabled", enabled).apply()
            VOICE_LIGHT -> prefs.edit().putBoolean("mayra_voice_light_enabled", enabled).apply()
            WHATSAPP_IMPORTANT -> prefs.edit().putBoolean("mayra_whatsapp_important_enabled", enabled).apply()
        }
    }

    fun label(control: String): String = when (control) {
        CAMERA -> "📷 Camera Access"
        INCOMING_CALLS -> "📞 Incoming Call Assistant"
        THREE_D_CHARACTER -> "🧍 3D Character"
        VOICE_LIGHT -> "✨ Voice Light"
        WHATSAPP_IMPORTANT -> "💬 WhatsApp Important Information"
        else -> control
    }

    fun voiceCommands(): List<String> = listOf(
        "মায়রা ক্যামেরা চালু করো / বন্ধ করো",
        "মায়রা কল অ্যাসিস্ট্যান্ট চালু করো / বন্ধ করো",
        "মায়রা থ্রিডি অ্যানিমেশন চালু করো / বন্ধ করো",
        "মায়রা ভয়েস লাইট চালু করো / বন্ধ করো",
        "মায়রা গুরুত্বপূর্ণ তথ্য WhatsApp-এ পাঠানো চালু করো / বন্ধ করো",
        "মায়রা এই গুরুত্বপূর্ণ তথ্যটা WhatsApp-এ পাঠাও"
    )

    fun incomingCallRule(): String =
        "Incoming Call Assistant ON থাকলে Android-এর অনুমোদিত call/telecom capability ব্যবহার করা যাবে। Call answer/receive-এর জন্য OS, default-phone/telecom এবং user permission লাগতে পারে; Mayra গোপনে call intercept বা record করবে না।"

    fun whatsappRule(): String =
        MayraWhatsAppImportantChannel.ownerControlRule() + " Direct WhatsApp delivery requires an authorized integration; otherwise Mayra prepares the message only."

    fun voiceLightRule(): String =
        "Voice Light শুধু Mayra কথা বলার সময় visual indication দেবে; microphone/camera covertly চালু করবে না।"

    fun threeDRule(): String =
        "3D Character একটি optional visual presentation layer; এটি biometric identity inference বা covert sensor access করবে না।"
}
