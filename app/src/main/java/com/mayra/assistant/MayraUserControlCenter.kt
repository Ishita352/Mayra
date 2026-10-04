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
    const val VOICE_COMMAND_ACCESS = "voice_command_access"
    const val LOCKED_PHONE_ACTIVE = "locked_phone_active"
    const val SILENT_MODE = "silent_mode_behavior"
    const val MAYRA_VOLUME = "mayra_volume"

    data class State(
        val camera:Boolean,val incomingCalls:Boolean,val threeDCharacter:Boolean,
        val voiceLight:Boolean,val whatsappImportant:Boolean,val voiceCommandAccess:Boolean,
        val lockedPhoneActive:Boolean,val silentMode:Boolean,val volumePercent:Int
    )

    fun state(prefs:android.content.SharedPreferences)=State(
        FeatureToggleRegistry.isEnabled(prefs,FeatureToggleRegistry.CAMERA),
        FeatureToggleRegistry.isEnabled(prefs,FeatureToggleRegistry.INCOMING_CALL_ASSISTANT),
        prefs.getBoolean("mayra_3d_character_enabled",false),
        prefs.getBoolean("mayra_voice_light_enabled",true),
        prefs.getBoolean("mayra_whatsapp_important_enabled",true),
        FeatureToggleRegistry.isEnabled(prefs,FeatureToggleRegistry.VOICE_COMMAND),
        LockModePolicy.isEnabled(prefs),
        prefs.getBoolean("mayra_silent_mode_behavior",true),
        volume(prefs)
    )

    fun set(prefs:android.content.SharedPreferences,control:String,enabled:Boolean) {
        when(control) {
            CAMERA -> FeatureToggleRegistry.setEnabled(prefs,FeatureToggleRegistry.CAMERA,enabled)
            INCOMING_CALLS -> FeatureToggleRegistry.setEnabled(prefs,FeatureToggleRegistry.INCOMING_CALL_ASSISTANT,enabled)
            THREE_D_CHARACTER -> prefs.edit().putBoolean("mayra_3d_character_enabled",enabled).apply()
            VOICE_LIGHT -> prefs.edit().putBoolean("mayra_voice_light_enabled",enabled).apply()
            WHATSAPP_IMPORTANT -> prefs.edit().putBoolean("mayra_whatsapp_important_enabled",enabled).apply()
            VOICE_COMMAND_ACCESS -> FeatureToggleRegistry.setEnabled(prefs,FeatureToggleRegistry.VOICE_COMMAND,enabled)
            LOCKED_PHONE_ACTIVE -> LockModePolicy.setEnabled(prefs, enabled)
            SILENT_MODE -> prefs.edit().putBoolean("mayra_silent_mode_behavior",enabled).apply()
            MAYRA_VOLUME -> Unit
        }
    }

    fun setVolume(prefs:android.content.SharedPreferences,percent:Int):Int {
        val value=percent.coerceIn(0,100)
        prefs.edit().putInt("mayra_volume_percent",value).apply()
        return value
    }
    fun volume(prefs:android.content.SharedPreferences):Int =
        prefs.getInt("mayra_volume_percent",70).coerceIn(0,100)

    fun label(control:String)=when(control) {
        CAMERA -> "📷 Camera Access"
        INCOMING_CALLS -> "📞 Incoming Call Assistant"
        THREE_D_CHARACTER -> "🧍 3D Character"
        VOICE_LIGHT -> "✨ Voice Light"
        WHATSAPP_IMPORTANT -> "💬 WhatsApp Important Information"
        VOICE_COMMAND_ACCESS -> "🎙️ Voice Command Access"
        LOCKED_PHONE_ACTIVE -> "🔒 Locked Phone Activity"
        SILENT_MODE -> "🔇 Silent Mode Behavior"
        MAYRA_VOLUME -> "🔊 Mayra Volume"
        else -> control
    }

    fun voiceCommands()=listOf(
        "মায়রা ক্যামেরা চালু করো / বন্ধ করো",
        "মায়রা কল অ্যাসিস্ট্যান্ট চালু করো / বন্ধ করো",
        "মায়রা থ্রিডি অ্যানিমেশন চালু করো / বন্ধ করো",
        "মায়রা ভয়েস লাইট চালু করো / বন্ধ করো",
        "মায়রা গুরুত্বপূর্ণ তথ্য WhatsApp-এ পাঠানো চালু করো / বন্ধ করো",
        "মায়রা এই গুরুত্বপূর্ণ তথ্যটা WhatsApp-এ পাঠাও",
        "মায়রা voice command access চালু করো / বন্ধ করো",
        "মায়রা ফোন lock থাকা অবস্থায় active থাকো / বন্ধ থাকো",
        "মায়রা silent mode behavior চালু করো / বন্ধ করো",
        "মায়রা volume বাড়াও / কমাও"
    )

    fun incomingCallRule()="Incoming Call Assistant ON থাকলে Android-এর অনুমোদিত call/telecom capability ব্যবহার করা যাবে। Call answer/receive-এর জন্য OS, default-phone/telecom এবং user permission লাগতে পারে; Mayra গোপনে call intercept বা record করবে না."
    fun whatsappRule()=MayraWhatsAppImportantChannel.ownerControlRule()+" Direct WhatsApp delivery requires an authorized integration; otherwise Mayra prepares the message only."
    fun voiceLightRule()="Voice Light শুধু Mayra কথা বলার সময় visual indication দেবে; microphone/camera covertly চালু করবে না."
    fun threeDRule()="3D Character একটি optional visual presentation layer; এটি biometric identity inference বা covert sensor access করবে না."
    fun silentModeRule()="Phone silent থাকলে Mayra voice input শুনতে পারবে, কিন্তু voice response/TTS বন্ধ রাখবে."
    fun lockedPhoneRule()="Locked Phone Activity Owner-controlled। ON থাকলে কেবল অনুমোদিত/নিরাপদ locked-device functions সক্রিয় থাকবে; sensitive commands, authentication bypass বা covert access নয়."
    fun volumeRule()="Mayra volume adjustment শুধু Android-এর অনুমোদিত audio stream ব্যবহার করবে এবং Owner-এর voice command/visible control দিয়ে বাড়ানো-কমানো যাবে."
}
