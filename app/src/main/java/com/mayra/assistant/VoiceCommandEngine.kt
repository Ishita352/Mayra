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
        SHOW_HELP
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

        return when {
            settings.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.OPEN_SETTINGS, "বস, Settings খুলছি।")
            browser.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.OPEN_BROWSER, "বস, Browser খুলছি।")
            camera.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.OPEN_CAMERA, "বস, Camera খুলছি।")
            time.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SHOW_TIME, "বস, এখনকার সময় দেখাচ্ছি।")
            help.any { text.contains(it) } -> VoiceCommandResult(true, VoiceCommandResult.Action.SHOW_HELP, "বস, আমি এখন Settings, Browser, Camera, সময় এবং Help-এর মতো নিরাপদ কমান্ড চালাতে পারি।")
            else -> VoiceCommandResult(false, VoiceCommandResult.Action.NONE, "বস, এই কমান্ডটি এখনো আমার Command Engine-এ যোগ করা হয়নি।")
        }
    }
}
