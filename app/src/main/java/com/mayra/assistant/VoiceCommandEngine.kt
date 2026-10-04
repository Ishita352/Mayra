package com.mayra.assistant

import java.util.Locale

data class VoiceCommandResult(
    val recognized: Boolean,
    val action: Action,
    val response: String
) {
    enum class Action {
        NONE,
        OPEN_SETTINGS,
        OPEN_BROWSER,
        OPEN_CAMERA,
        SHOW_TIME,
        SHOW_HELP,
        SET_VOICE_COMMAND,
        SET_CAMERA,
        SET_INCOMING_CALL_ASSISTANT,
        REPLY_WHATSAPP
    }
}

object VoiceCommandEngine {
    fun parse(spoken: String): VoiceCommandResult {
        val text = spoken.trim().lowercase(Locale.ROOT)
        if (text.isBlank()) return VoiceCommandResult(false, VoiceCommandResult.Action.NONE, "বস, আমি কিছু শুনতে পাইনি।")

        val settings = listOf("settings", "setting", "সেটিং", "सेटिंग", "सेटिंग्स")
        val browser = listOf("browser", "ব্রাউজার", "ब्राउज़र", "chrome", "ক্রোম")
        val camera = listOf("camera", "ক্যামেরা", "कैमरा")
        val time = listOf("time", "সময়", "সময়", "কয়টা বাজে", "কয়টা বাজে", "समय", "कितने बजे")
        val help = listOf("help", "কি করতে পার", "কি করতে পারো", "কি কাজ", "क्या कर सकती", "क्या कर सकते", "मदद")
        val voiceOn = listOf("ভয়েস কমান্ড চালু", "ভয়েস কমান্ড চালু", "voice command on", "enable voice command", "वॉइस कमांड चालू")
        val voiceOff = listOf("ভয়েস কমান্ড বন্ধ", "ভয়েস কমান্ড বন্ধ", "voice command off", "disable voice command", "वॉइस कमांड बंद")
        val cameraOn = listOf("ক্যামেরা চালু", "camera on", "enable camera", "कैमरा चालू")
        val cameraOff = listOf("ক্যামেরা বন্ধ", "camera off", "disable camera", "कैमरा बंद")
        val callOn = listOf("ইনকামিং কল অ্যাসিস্ট্যান্ট চালু", "incoming call assistant on", "enable incoming call assistant", "इनकमिंग कॉल असिस्टेंट चालू")
        val callOff = listOf("ইনকামিং কল অ্যাসিস্ট্যান্ট বন্ধ", "incoming call assistant off", "disable incoming call assistant", "इनकमिंग कॉल असिस्टेंट बंद")
        val reply = listOf("reply whatsapp", "whatsapp reply", "হোয়াটসঅ্যাপ রিপ্লাই", "হোয়াটসঅ্যাপ রিপ্লাই", "রিপ্লাই দাও", "reply দাও")

        return when {
            voiceOff.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SET_VOICE_COMMAND, "বস, Voice Command বন্ধ করার নির্দেশ পেয়েছি।")
            voiceOn.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SET_VOICE_COMMAND, "বস, Voice Command চালু করার নির্দেশ পেয়েছি।")
            cameraOff.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SET_CAMERA, "বস, Camera বন্ধ করার নির্দেশ পেয়েছি।")
            cameraOn.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SET_CAMERA, "বস, Camera চালু করার নির্দেশ পেয়েছি।")
            callOff.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SET_INCOMING_CALL_ASSISTANT, "বস, Incoming Call Assistant বন্ধ করার নির্দেশ পেয়েছি।")
            callOn.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SET_INCOMING_CALL_ASSISTANT, "বস, Incoming Call Assistant চালু করার নির্দেশ পেয়েছি।")
            reply.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.REPLY_WHATSAPP, "বস, WhatsApp reply-এর জন্য আপনার কথাটি বলুন।")
            reply.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.REPLY_WHATSAPP, "বস, WhatsApp-এর জন্য reply text বলুন।")
            settings.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.OPEN_SETTINGS, "বস, Settings খুলছি।")
            browser.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.OPEN_BROWSER, "বস, Browser খুলছি।")
            camera.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.OPEN_CAMERA, "বস, Camera খুলছি।")
            time.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SHOW_TIME, "বস, এখনকার সময় দেখাচ্ছি।")
            help.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SHOW_HELP, "বস, আমি ফোনের Settings, Browser, Camera, সময়, Help এবং ফোন-কম্পিউটার pairing/status/browser/file command বুঝতে পারি। কম্পিউটারে কাজ করাতে Windows agent pairing সম্পূর্ণ করতে হবে।")
            else -> VoiceCommandResult(false, VoiceCommandResult.Action.NONE, "বস, এই কমান্ডটি এখনো আমার Command Engine-এ যোগ করা হয়নি।")
        }
    }
}
