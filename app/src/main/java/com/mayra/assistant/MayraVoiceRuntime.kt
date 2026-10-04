package com.mayra.assistant

import java.util.Locale

/**
 * Runtime coordinator for Mayra voice input.
 *
 * Keeps voice routing in one place: language detection, interruption,
 * offline-first workflow commands, and Owner-facing response style.
 * This is a routing/policy layer; Android SpeechRecognizer/TTS remain
 * responsible for actual audio capture and playback.
 */
object MayraVoiceRuntime {
    enum class Language { BENGALI, HINDI, ENGLISH, UNKNOWN }

    enum class Action {
        NONE,
        STOP_SPEAKING,
        OFFLINE_MODE,
        RUN_OFFLINE_TASKS,
        QUEUE_UPLOAD
    }

    data class Result(
        val recognized: Boolean,
        val action: Action,
        val language: Language,
        val response: String
    )

    fun detectLanguage(spoken: String): Language {
        val text = spoken.trim()
        if (text.isBlank()) return Language.UNKNOWN
        return when {
            text.any { it in '\u0980'..'\u09FF' } -> Language.BENGALI
            text.any { it in '\u0900'..'\u097F' } -> Language.HINDI
            text.any { it.isLetter() } -> Language.ENGLISH
            else -> Language.UNKNOWN
        }
    }

    fun route(spoken: String): Result {
        val text = spoken.trim().lowercase(Locale.ROOT)
        if (text.isBlank()) {
            return Result(false, Action.NONE, Language.UNKNOWN, "বস, আমি কিছু শুনতে পাইনি।")
        }

        val language = detectLanguage(spoken)

        val stop = listOf(
            "থামো", "চুপ করো", "কথা বন্ধ করো", "মায়রা থামো",
            "stop speaking", "stop talking", "be quiet", "quiet",
            "चुप करो", "बोलना बंद करो", "रुको"
        )
        if (stop.any { text.contains(it) }) {
            return Result(true, Action.STOP_SPEAKING, language, "বস, আমি কথা বলা থামাচ্ছি।")
        }

        val offline = listOf(
            "অফলাইনে কাজ করো", "offline কাজ", "work offline",
            "offline mode", "मायरा ऑफलाइन काम करो"
        )
        if (offline.any { text.contains(it) }) {
            return Result(true, Action.OFFLINE_MODE, language, "বস, Offline-first কাজের মোড চালু হলো।")
        }

        val runOffline = listOf(
            "অফলাইনের কাজ চালাও", "অফলাইন কাজ শুরু", "offline task",
            "run offline tasks", "ऑफलाइन काम शुरू"
        )
        if (runOffline.any { text.contains(it) }) {
            return Result(true, Action.RUN_OFFLINE_TASKS, language, "বস, অনুমোদিত offline কাজের workflow চালানোর জন্য প্রস্তুত।")
        }

        val queue = listOf(
            "আপলোড পরে করো", "পরে আপলোড করো", "upload later",
            "queue upload", "बाद में अपलोड करो"
        )
        if (queue.any { text.contains(it) }) {
            return Result(true, Action.QUEUE_UPLOAD, language, "বস, upload পরে করার জন্য queue করা হবে; network ও Owner approval ছাড়া upload হবে না।")
        }

        return Result(false, Action.NONE, language, "")
    }

    fun ownerResponse(message: String): String {
        val trimmed = message.trim()
        if (trimmed.isBlank()) return "বস, আমি প্রস্তুত।"
        return if (trimmed.startsWith("বস,")) trimmed else "বস, $trimmed"
    }

    fun offlineDecision(
        task: MayraOfflineVoiceWorkflow.Task,
        online: Boolean,
        ownerApproved: Boolean = false
    ): MayraOfflineVoiceWorkflow.Decision =
        MayraOfflineVoiceWorkflow.decide(task, online, ownerApproved)
}
