package com.mayra.assistant

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.app.KeyguardManager
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Locale

/**
 * Keeps Mayra's voice command listener alive outside the Activity UI.
 *
 * It is deliberately a foreground microphone service: Android requires a
 * user-visible foreground service for long-running microphone capture.
 * It must be started while Mayra is visible and the user has enabled Voice
 * Command. Swiping Mayra away from Recents does not intentionally stop this
 * service; Force Stop, permission revocation, or Android/OEM battery policy
 * can still stop it.
 */
class MayraBackgroundVoiceService : Service() {
    companion object {
        const val ACTION_START = "com.mayra.assistant.action.START_BACKGROUND_VOICE"
        const val ACTION_STOP = "com.mayra.assistant.action.STOP_BACKGROUND_VOICE"
        const val ACTION_COMMAND = "com.mayra.assistant.action.BACKGROUND_COMMAND"
        const val EXTRA_SPOKEN = "spoken"
        private const val CHANNEL_ID = "mayra_background_voice"
        private const val NOTIFICATION_ID = 9401
    }

    private val prefs by lazy { getSharedPreferences("mayra_secure", MODE_PRIVATE) }
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var restarting = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TextToSpeech(this) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopListening()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (!canRun()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, notification())
        startListening()
        return START_STICKY
    }

    private fun canRun(): Boolean {
        val baseAllowed =
            prefs.getBoolean("master_on", false) &&
                FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND) &&
                prefs.getBoolean("owner_verified", false) &&
                prefs.getBoolean("owner_command_authorized", false) &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!baseAllowed) return false
        val locked = getSystemService(KeyguardManager::class.java)?.isKeyguardLocked == true
        return !locked || LockModePolicy.isEnabled(prefs)
    }

    private fun startListening() {
        if (!canRun() || !SpeechRecognizer.isRecognitionAvailable(this)) return
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this)
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() { scheduleRestart() }
                override fun onError(error: Int) { scheduleRestart() }
                override fun onResults(results: Bundle?) {
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()?.trim()
                    if (!spoken.isNullOrBlank()) handleCommand(spoken)
                    scheduleRestart()
                }
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, recognitionLanguage())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, recognitionLanguage())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        try {
            recognizer?.startListening(intent)
        } catch (_: Exception) {
            scheduleRestart()
        }
    }

    private fun recognitionLanguage(): String {
        return when (prefs.getString("mayra_voice_language", "auto")?.lowercase(Locale.ROOT)) {
            "bn", "bengali", "বাংলা" -> "bn-IN"
            "hi", "hindi", "हिन्दी", "हिंदी" -> "hi-IN"
            "en", "english" -> "en-IN"
            else -> Locale.getDefault().toLanguageTag().ifBlank { "en-IN" }
        }
    }

    private fun scheduleRestart() {
        if (restarting || !canRun()) return
        restarting = true
        android.os.Handler(mainLooper).postDelayed({
            restarting = false
            startListening()
        }, 900L)
    }

    private fun handleCommand(spoken: String) {
        if (!canRun()) {
            stopListening()
            return
        }
        val locked = getSystemService(KeyguardManager::class.java)?.isKeyguardLocked == true
        if (locked) {
            val decision = MayraLockedPhoneVoiceGate.decide(
                prefs,
                ownerVerified = prefs.getBoolean("owner_verified", false) &&
                    prefs.getBoolean("owner_command_authorized", false),
                ownerCommandAuthorized = prefs.getBoolean("owner_command_authorized", false),
                masterOn = prefs.getBoolean("master_on", false),
                locked = true,
                task = spoken
            )
            if (decision != MayraLockedPhoneVoiceGate.Decision.ALLOW_LIMITED_VOICE) {
                speak("This command is not allowed while the phone is locked.")
                return
            }
        }
        sendBroadcast(Intent(ACTION_COMMAND).setPackage(packageName).putExtra(EXTRA_SPOKEN, spoken))
        val result = MayraBackgroundCommandRouter.route(this, spoken)
        if (result.handled) {
            speak(result.response)
            if (!getSharedPreferences("mayra_secure", MODE_PRIVATE).getBoolean("master_on", false) ||
                !FeatureToggleRegistry.isEnabled(getSharedPreferences("mayra_secure", MODE_PRIVATE), FeatureToggleRegistry.VOICE_COMMAND)) {
                stopListening()
            }
        }
    }

    private fun speak(message: String) {
        val audioManager = getSystemService(AudioManager::class.java)
        if (prefs.getBoolean("mayra_silent_mode_behavior", true) && audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        val selected = when (prefs.getString("mayra_voice_language", "auto")?.lowercase(Locale.ROOT)) {
            "bn", "bengali", "বাংলা" -> Locale("bn", "IN")
            "hi", "hindi", "हिन्दी", "हिंदी" -> Locale("hi", "IN")
            "en", "english" -> Locale("en", "IN")
            else -> when {
                message.any { it in '\u0980'..'\u09FF' } -> Locale("bn", "IN")
                message.any { it in '\u0900'..'\u097F' } -> Locale("hi", "IN")
                else -> Locale("en", "IN")
            }
        }
        tts?.setLanguage(selected)
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "mayra_background")
    }

    private fun stopListening() {
        recognizer?.cancel()
    }

    private fun notification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Mayra voice command active")
            .setContentText("Mayra is listening for your enabled voice commands.")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Mayra Background Voice",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    override fun onDestroy() {
        stopListening()
        recognizer?.destroy()
        try { phoneServer?.close() } catch (_: Exception) {}
        phoneServer = null
        phoneServerThread?.interrupt()
        phoneServerThread = null
        recognizer = null
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
