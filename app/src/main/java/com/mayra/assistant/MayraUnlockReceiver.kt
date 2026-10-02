package com.mayra.assistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import java.util.Locale

class MayraUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_USER_PRESENT) return
        val prefs = context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("owner_verified", false)) return

        val pending = goAsync()
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("bn", "IN")
                tts.speak(
                    "স্বাগতম গোপাল বসাক। মায়রা প্রস্তুত আছে। আপনার আজকের কাজ শুরু করা যাক।",
                    TextToSpeech.QUEUE_FLUSH, null, "mayra_owner_welcome"
                )
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    tts.shutdown()
                    pending.finish()
                }, 3500)
            } else {
                pending.finish()
            }
        }
    }
}
