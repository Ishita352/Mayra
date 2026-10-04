package com.mayra.assistant

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import java.io.File
import java.util.Locale
import kotlin.concurrent.thread

object MayraWhatsAppVoiceMessage {
    const val INTRO = "হ্যালো, আমি মায়রা। আমি গোপালের পার্সোনাল অ্যাসিস্ট্যান্ট। বস আমাকে এই কথা আপনাকে বলার জন্য বলেছেন।"

    fun createAndShare(context: Context, requestedMessage: String, onResult: (String) -> Unit) {
        val clean = requestedMessage.trim()
        if (clean.isEmpty()) {
            onResult("বস, voice message-এর কথাটি বলুন।")
            return
        }
        val outputDir = File(context.cacheDir, "mayra_whatsapp_voice")
        outputDir.mkdirs()
        val output = File(outputDir, "mayra_voice_message.wav")
        thread {
            var tts: TextToSpeech? = null
            try {
                tts = TextToSpeech(context.applicationContext) { status ->
                    if (status != TextToSpeech.SUCCESS) {
                        onResult("Voice message তৈরি করা যায়নি: TTS unavailable.")
                        return@TextToSpeech
                    }
                    val languageResult = tts?.setLanguage(Locale("bn", "IN"))
                    if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                        languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                    }
                    val text = "$INTRO $clean"
                    val result = tts?.synthesizeToFile(text, null, output, "mayra_whatsapp_voice")
                    if (result == TextToSpeech.SUCCESS) {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "audio/wav"
                            putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(output))
                            setPackage("com.whatsapp")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(Intent.createChooser(share, "Mayra → WhatsApp Voice Message"))
                            onResult("বস, Mayra-র পরিচয়সহ voice message WhatsApp-এ দেওয়ার জন্য প্রস্তুত। Recipient ও Send আপনার নিয়ন্ত্রণে থাকবে।")
                        } catch (_: Exception) {
                            onResult("WhatsApp খোলা যায়নি। WhatsApp ইনস্টল আছে কি না পরীক্ষা করুন।")
                        }
                    } else {
                        onResult("Voice message audio তৈরি করা যায়নি।")
                    }
                    tts?.shutdown()
                }
            } catch (e: Exception) {
                tts?.shutdown()
                onResult("Voice message workflow ব্যর্থ: ${e.message ?: "unknown error"}")
            }
        }
    }
}
