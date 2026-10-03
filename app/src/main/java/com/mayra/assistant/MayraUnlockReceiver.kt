package com.mayra.assistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.os.Handler
import android.os.Looper
import java.util.Locale

class MayraUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_USER_PRESENT) return
        val prefs = context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("owner_verified", false)) return
        if (!prefs.getBoolean("master_on", true)) return
        if (!FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND)) return

        val pending = goAsync()
        lateinit var speech: TextToSpeech
        speech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                speech.language = Locale("bn", "IN")
                speech.speak(
                    "স্বাগতম গোপাল বসাক। মায়রা প্রস্তুত আছে। আপনার আজকের কাজ শুরু করা যাক।",
                    TextToSpeech.QUEUE_FLUSH, null, "mayra_owner_welcome"
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    speech.shutdown()
                    pending.finish()
                }, 3500)
            } else {
                speech.shutdown()
                pending.finish()
            }
        }
    }
}
