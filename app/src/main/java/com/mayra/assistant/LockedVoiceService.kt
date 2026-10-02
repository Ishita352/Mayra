package com.mayra.assistant

import android.app.*
import android.content.*
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import java.util.Locale

class LockedVoiceService : Service() {
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private var listening = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(9101, Notification.Builder(this, "mayra_locked_voice")
            .setContentTitle("Mayra — Locked Voice Mode")
            .setContentText("লক অবস্থায় শুধু নির্দিষ্ট wake phrase শোনা হচ্ছে")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build())

        tts = TextToSpeech(this) { }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                listening = false
                val phrases = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                val hit = phrases.firstOrNull { isAllowedPhrase(it) }
                if (hit != null && isPhoneLocked()) {
                    speakFor(hit)
                }
                restartIfLocked()
            }
            override fun onError(error: Int) { listening = false; restartIfLocked() }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        })
        restartIfLocked()
    }

    private fun isAllowedPhrase(text: String): Boolean {
        val s = text.lowercase(Locale.getDefault())
            .replace(",", " ").replace("?", " ").replace("।", " ")
            .replace(Regex("\\s+"), " ").trim()
        return s.contains("মায়রা") && (
            s.contains("কাহাপে হো") || s.contains("কাহাঁপে হো") ||
            s.contains("কাহা পে হো") || s.contains("where are you") ||
            s.contains("where are you mayra") || s.contains("কোথায় আছ") ||
            s.contains("কোথায় আছ") || s.contains("तुम कहाँ हो") ||
            s.contains("कहां हो") || s.contains("कहाँ हो")
        )
    }

    private fun isPhoneLocked(): Boolean =
        (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked

    private fun restartIfLocked() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isPhoneLocked() && !listening) startListening()
        }, 500)
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        listening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        try { recognizer.startListening(intent) } catch (_: Exception) { listening = false }
    }

    private fun speakFor(phrase: String) {
        val hindi = phrase.contains("कहाँ") || phrase.contains("कहां")
        val english = phrase.lowercase(Locale.getDefault()).contains("where are you")
        val locale = when {
            hindi -> Locale("hi", "IN")
            english -> Locale.US
            else -> Locale("bn", "IN")
        }
        tts.language = locale
        val reply = when {
            hindi -> "मैं यहीं हूँ। फोन लॉक है, इसलिए मैं कोई दूसरा काम नहीं करूँगी।"
            english -> "I am here. Your phone is locked, so I will not do anything else."
            else -> "আমি এখানেই আছি। ফোন লক আছে, তাই আমি আর কোনো কাজ করব না।"
        }
        tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "mayra_locked_reply")
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT || intent?.action == Intent.ACTION_SCREEN_ON) {
                try { recognizer.stopListening() } catch (_: Exception) {}
                listening = false
            } else if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                restartIfLocked()
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(
                "mayra_locked_voice", "Mayra Locked Voice",
                NotificationManager.IMPORTANCE_LOW
            ))
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        try { unregisterReceiver(screenReceiver) } catch (_: Exception) {}
        try { recognizer.destroy() } catch (_: Exception) {}
        try { tts.shutdown() } catch (_: Exception) {}
        super.onDestroy()
    }
}
